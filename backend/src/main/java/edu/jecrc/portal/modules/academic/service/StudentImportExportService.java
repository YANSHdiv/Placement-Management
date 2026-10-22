package edu.jecrc.portal.modules.academic.service;

import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.dto.*;
import edu.jecrc.portal.modules.academic.entity.*;
import edu.jecrc.portal.modules.academic.repository.*;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.Role;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentImportExportService {

    private final StudentImportRepository importRepository;
    private final StudentImportRowRepository importRowRepository;
    private final StudentRepository studentRepository;
    private final AcademicProgramRepository programRepository;
    private final AcademicBatchRepository batchRepository;
    private final StudentQualificationRepository qualificationRepository;
    private final UserRepository userRepository;
    private final AcademicSecurityService academicSecurityService;
    private final AuditService auditService;

    @Transactional
    public ImportPreviewResponse previewImport(MultipartFile file, User caller) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "import.csv";
        String extension = getFileExtension(originalName).toLowerCase();

        if (!extension.equals("csv") && !extension.equals("xlsx")) {
            throw new IllegalArgumentException("Invalid file format. Only .csv and .xlsx files are supported.");
        }

        List<Map<String, Object>> parsedRows;
        try {
            if (extension.equals("csv")) {
                parsedRows = parseCsv(file.getInputStream());
            } else {
                parsedRows = parseExcel(file.getInputStream());
            }
        } catch (Exception e) {
            log.error("Failed to parse student import file", e);
            throw new IllegalArgumentException("Error parsing file: " + e.getMessage());
        }

        StudentImport studentImport = StudentImport.builder()
                .importedBy(caller)
                .fileName(originalName)
                .fileType(extension.toUpperCase())
                .status("PREVIEWED")
                .totalRows(parsedRows.size())
                .build();

        studentImport = importRepository.save(studentImport);

        int validRows = 0;
        int errorRows = 0;
        Set<String> fileStudentIds = new HashSet<>();
        Set<String> fileRollNumbers = new HashSet<>();
        Set<String> fileEmails = new HashSet<>();

        List<ImportPreviewResponse.ImportRowPreviewDto> sampleRows = new ArrayList<>();
        List<StudentImportRow> rowEntities = new ArrayList<>();

        for (int i = 0; i < parsedRows.size(); i++) {
            int rowNum = i + 1;
            Map<String, Object> data = parsedRows.get(i);
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // 1. Validate mandatory fields
            String studentId = getString(data, "university_student_id");
            String rollNum = getString(data, "roll_number");
            String firstName = getString(data, "first_name");
            String lastName = getString(data, "last_name");
            String email = getString(data, "university_email");
            String progCode = getString(data, "program_code");
            String gradYearStr = getString(data, "graduation_year");
            String semStr = getString(data, "current_semester");

            if (studentId == null) errors.add("Missing university_student_id");
            if (rollNum == null) errors.add("Missing roll_number");
            if (firstName == null) errors.add("Missing first_name");
            if (lastName == null) errors.add("Missing last_name");
            if (email == null) errors.add("Missing university_email");
            if (progCode == null) errors.add("Missing program_code");
            if (gradYearStr == null) errors.add("Missing graduation_year");
            if (semStr == null) errors.add("Missing current_semester");

            // 2. Validate in-file duplicates
            if (studentId != null) {
                if (fileStudentIds.contains(studentId)) {
                    errors.add("Duplicate university_student_id within file: " + studentId);
                } else {
                    fileStudentIds.add(studentId);
                }
            }
            if (rollNum != null) {
                if (fileRollNumbers.contains(rollNum)) {
                    errors.add("Duplicate roll_number within file: " + rollNum);
                } else {
                    fileRollNumbers.add(rollNum);
                }
            }
            if (email != null) {
                if (fileEmails.contains(email.toLowerCase())) {
                    errors.add("Duplicate university_email within file: " + email);
                } else {
                    fileEmails.add(email.toLowerCase());
                }
            }

            // 3. Validate Program and Batch existence & HOD department scope
            if (progCode != null) {
                Optional<AcademicProgram> programOpt = programRepository.findByProgramCode(progCode.toUpperCase());
                if (programOpt.isEmpty()) {
                    errors.add("Academic program code not found: " + progCode);
                } else {
                    AcademicProgram prog = programOpt.get();
                    // Scope check for HOD
                    Set<String> roles = caller.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
                    if (roles.contains("HOD") && !roles.contains("SUPER_ADMIN") && !roles.contains("TPO_ADMIN")) {
                        List<Integer> scopedDepts = academicSecurityService.getScopedDepartmentIds(caller.getId());
                        if (!scopedDepts.contains(prog.getDepartment().getId())) {
                            errors.add("Program '" + progCode + "' belongs to a department outside your assigned scope");
                        }
                    }

                    if (gradYearStr != null) {
                        try {
                            int gradYear = Integer.parseInt(gradYearStr);
                            Optional<AcademicBatch> batchOpt = batchRepository.findByProgramIdAndGraduationYear(prog.getId(), gradYear);
                            if (batchOpt.isEmpty()) {
                                errors.add("No batch found for program " + progCode + " graduating in year " + gradYear);
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Invalid graduation_year integer format: " + gradYearStr);
                        }
                    }
                }
            }

            // 4. Validate numerical marks if provided
            validatePercentage(data, "class_10_percentage", errors);
            validatePercentage(data, "class_12_percentage", errors);

            // 5. Check if record already exists in database
            if (studentId != null && studentRepository.existsByUniversityStudentId(studentId)) {
                warnings.add("Record already exists in database. Will be updated if permitted.");
            }

            boolean isValid = errors.isEmpty();
            if (isValid) validRows++;
            else errorRows++;

            StudentImportRow rowEntity = StudentImportRow.builder()
                    .studentImport(studentImport)
                    .rowNumber(rowNum)
                    .rowData(data)
                    .isValid(isValid)
                    .errors(errors.isEmpty() ? null : errors)
                    .warnings(warnings.isEmpty() ? null : warnings)
                    .status(isValid ? "PENDING" : "REJECTED")
                    .build();

            rowEntities.add(rowEntity);

            if (sampleRows.size() < 10) {
                sampleRows.add(ImportPreviewResponse.ImportRowPreviewDto.builder()
                        .rowNumber(rowNum)
                        .data(data)
                        .isValid(isValid)
                        .errors(errors)
                        .warnings(warnings)
                        .build());
            }
        }

        importRowRepository.saveAll(rowEntities);

        studentImport.setValidRows(validRows);
        studentImport.setErrorRows(errorRows);
        importRepository.save(studentImport);

        return ImportPreviewResponse.builder()
                .importId(studentImport.getId())
                .fileName(studentImport.getFileName())
                .fileType(studentImport.getFileType())
                .totalRows(studentImport.getTotalRows())
                .validRows(validRows)
                .errorRows(errorRows)
                .sampleRows(sampleRows)
                .createdAt(studentImport.getCreatedAt())
                .build();
    }

    @Transactional
    public ImportCommitResponse commitImport(ImportCommitRequest req, User caller) {
        StudentImport studentImport = importRepository.findById(req.getImportId())
                .orElseThrow(() -> new ResourceNotFoundException("Import preview not found with ID: " + req.getImportId()));

        if (!studentImport.getStatus().equals("PREVIEWED")) {
            throw new IllegalStateException("Import has already been processed with status: " + studentImport.getStatus());
        }

        List<StudentImportRow> rows = importRowRepository.findByStudentImportIdOrderByRowNumberAsc(studentImport.getId());

        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (StudentImportRow row : rows) {
            if (!Boolean.TRUE.equals(row.getIsValid())) {
                skipped++;
                continue;
            }

            Map<String, Object> data = row.getRowData();
            String studentId = getString(data, "university_student_id");
            String rollNum = getString(data, "roll_number");
            String regNum = getString(data, "registration_number");
            String firstName = getString(data, "first_name");
            String lastName = getString(data, "last_name");
            String email = getString(data, "university_email");
            String personalEmail = getString(data, "personal_email");
            String phone = getString(data, "phone_number");
            String progCode = getString(data, "program_code");
            int gradYear = Integer.parseInt(getString(data, "graduation_year"));
            int currentSem = Integer.parseInt(getString(data, "current_semester"));
            String section = getString(data, "section");

            AcademicProgram program = programRepository.findByProgramCode(progCode.toUpperCase()).orElseThrow();
            AcademicBatch batch = batchRepository.findByProgramIdAndGraduationYear(program.getId(), gradYear).orElseThrow();
            Department department = program.getDepartment();

            Optional<Student> existingOpt = studentRepository.findByUniversityStudentId(studentId);

            if (existingOpt.isPresent()) {
                Student existing = existingOpt.get();
                // Rule: "Never silently overwrite verified academic records"
                if ("VERIFIED".equalsIgnoreCase(existing.getVerificationStatus())) {
                    log.warn("Skipping verified student {} to prevent unauthorized overwrite", studentId);
                    row.setStatus("SKIPPED");
                    skipped++;
                    continue;
                }

                if (Boolean.TRUE.equals(req.getUpdateExisting())) {
                    existing.setFirstName(firstName);
                    existing.setLastName(lastName);
                    existing.setUniversityEmail(email.toLowerCase());
                    existing.setPersonalEmail(personalEmail);
                    existing.setPhoneNumber(phone);
                    existing.setProgram(program);
                    existing.setDepartment(department);
                    existing.setBatch(batch);
                    existing.setCurrentSemester(currentSem);
                    existing.setSection(section);
                    studentRepository.save(existing);
                    row.setStatus("UPDATED");
                    updated++;
                } else {
                    row.setStatus("SKIPPED");
                    skipped++;
                }
            } else {
                User linkedUser = userRepository.findByEmailIgnoreCase(email).orElse(null);

                Student newStudent = Student.builder()
                        .universityStudentId(studentId)
                        .rollNumber(rollNum)
                        .registrationNumber(regNum)
                        .firstName(firstName)
                        .lastName(lastName)
                        .universityEmail(email.toLowerCase())
                        .personalEmail(personalEmail)
                        .phoneNumber(phone)
                        .program(program)
                        .department(department)
                        .batch(batch)
                        .currentSemester(currentSem)
                        .section(section != null ? section.toUpperCase() : "A")
                        .academicStatus("ENROLLED")
                        .verificationStatus("PENDING")
                        .user(linkedUser)
                        .build();

                newStudent = studentRepository.save(newStudent);

                // Insert qualifications if present
                createQualificationIfPresent(newStudent, "CLASS_10", data, "class_10_percentage");
                createQualificationIfPresent(newStudent, "CLASS_12", data, "class_12_percentage");

                row.setStatus("INSERTED");
                inserted++;
            }
        }

        studentImport.setStatus("COMMITTED");
        studentImport.setInsertedCount(inserted);
        studentImport.setUpdatedCount(updated);
        studentImport.setSkippedCount(skipped);
        studentImport.setCommittedAt(Instant.now());
        importRepository.save(studentImport);

        auditService.logSync(
                caller.getId(),
                caller.getEmail(),
                "STUDENT_IMPORT_COMMITTED",
                "StudentImport",
                studentImport.getId().toString(),
                Map.of("inserted", inserted, "updated", updated, "skipped", skipped),
                null
        );

        return ImportCommitResponse.builder()
                .importId(studentImport.getId())
                .status("COMMITTED")
                .totalRows(studentImport.getTotalRows())
                .insertedCount(inserted)
                .updatedCount(updated)
                .skippedCount(skipped)
                .errorRows(studentImport.getErrorRows())
                .committedAt(studentImport.getCommittedAt())
                .message("Successfully committed student import: " + inserted + " inserted, " + updated + " updated, " + skipped + " skipped.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<StudentImportRow> getImportErrors(UUID importId, User caller) {
        StudentImport studentImport = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException("Import not found with ID: " + importId));

        if (!studentImport.getImportedBy().getId().equals(caller.getId()) && !academicSecurityService.hasGlobalAcademicAccess(caller)) {
            throw new AccessDeniedException("Access Denied: You do not have permission to view errors for this import.");
        }

        return importRowRepository.findByStudentImportIdAndIsValidFalse(importId);
    }

    @Transactional(readOnly = true)
    public byte[] exportStudentsToExcel(Integer departmentId, Integer programId, Integer batchId, User caller) throws IOException {
        List<Student> students = fetchScopedStudentsForExport(departmentId, programId, batchId, caller);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "University Student ID", "Roll Number", "Registration Number",
                    "First Name", "Last Name", "University Email", "Phone Number",
                    "Department", "Program", "Batch", "Current Semester", "Section",
                    "CGPA", "Active Backlogs", "Academic Status", "Verification Status"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Student s : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getUniversityStudentId());
                row.createCell(1).setCellValue(s.getRollNumber());
                row.createCell(2).setCellValue(s.getRegistrationNumber() != null ? s.getRegistrationNumber() : "");
                row.createCell(3).setCellValue(s.getFirstName());
                row.createCell(4).setCellValue(s.getLastName());
                row.createCell(5).setCellValue(s.getUniversityEmail());
                row.createCell(6).setCellValue(s.getPhoneNumber() != null ? s.getPhoneNumber() : "");
                row.createCell(7).setCellValue(s.getDepartment() != null ? s.getDepartment().getDepartmentCode() : "");
                row.createCell(8).setCellValue(s.getProgram() != null ? s.getProgram().getProgramCode() : "");
                row.createCell(9).setCellValue(s.getBatch() != null ? s.getBatch().getBatchName() : "");
                row.createCell(10).setCellValue(s.getCurrentSemester());
                row.createCell(11).setCellValue(s.getSection() != null ? s.getSection() : "");
                if (s.getCgpa() != null) {
                    row.createCell(12).setCellValue(s.getCgpa().doubleValue());
                } else {
                    row.createCell(12).setCellValue("");
                }
                row.createCell(13).setCellValue(s.getActiveBacklogs());
                row.createCell(14).setCellValue(s.getAcademicStatus());
                row.createCell(15).setCellValue(s.getVerificationStatus());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private List<Student> fetchScopedStudentsForExport(Integer departmentId, Integer programId, Integer batchId, User caller) {
        Set<String> roles = caller.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
        if (roles.contains("HOD") && !roles.contains("SUPER_ADMIN") && !roles.contains("TPO_ADMIN")) {
            List<Integer> scopedDeptIds = academicSecurityService.getScopedDepartmentIds(caller.getId());
            if (departmentId != null && !scopedDeptIds.contains(departmentId)) {
                throw new AccessDeniedException("Access Denied: You cannot export student records outside your department scope.");
            }
            if (departmentId == null && !scopedDeptIds.isEmpty()) {
                departmentId = scopedDeptIds.get(0);
            }
        }

        final Integer targetDeptId = departmentId;
        return studentRepository.findAll((root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (targetDeptId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), targetDeptId));
            }
            if (programId != null) {
                predicates.add(cb.equal(root.get("program").get("id"), programId));
            }
            if (batchId != null) {
                predicates.add(cb.equal(root.get("batch").get("id"), batchId));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }

    private void createQualificationIfPresent(Student student, String type, Map<String, Object> data, String key) {
        String pctStr = getString(data, key);
        if (pctStr != null && !pctStr.isBlank()) {
            try {
                BigDecimal pct = new BigDecimal(pctStr);
                StudentQualification qual = StudentQualification.builder()
                        .student(student)
                        .qualificationType(type)
                        .boardOrUniversity("CBSE")
                        .passingYear(2022)
                        .markingScheme("PERCENTAGE")
                        .percentageOrCgpa(pct)
                        .verificationStatus("PENDING")
                        .build();
                qualificationRepository.save(qual);
            } catch (Exception e) {
                log.warn("Could not save initial qualification {} for student {}", type, student.getRollNumber());
            }
        }
    }

    private List<Map<String, Object>> parseCsv(InputStream in) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

            for (CSVRecord record : parser) {
                rows.add(new HashMap<>(record.toMap()));
            }
        }
        return rows;
    }

    private List<Map<String, Object>> parseExcel(InputStream in) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return rows;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim().toLowerCase());
            }

            DataFormatter formatter = new DataFormatter();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, Object> map = new HashMap<>();
                boolean hasContent = false;
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c);
                    String val = (cell != null) ? formatter.formatCellValue(cell).trim() : "";
                    map.put(headers.get(c), val);
                    if (!val.isBlank()) hasContent = true;
                }
                if (hasContent) {
                    rows.add(map);
                }
            }
        }
        return rows;
    }

    private void validatePercentage(Map<String, Object> data, String key, List<String> errors) {
        String val = getString(data, key);
        if (val != null && !val.isBlank()) {
            try {
                BigDecimal num = new BigDecimal(val);
                if (num.compareTo(BigDecimal.ZERO) < 0 || num.compareTo(BigDecimal.valueOf(100)) > 0) {
                    errors.add(key + " must be between 0 and 100");
                }
            } catch (NumberFormatException e) {
                errors.add(key + " must be a valid numeric percentage: " + val);
            }
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toLowerCase());
        if (val == null) return null;
        String s = val.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return (idx > 0) ? fileName.substring(idx + 1) : "";
    }
}
