package edu.jecrc.portal.modules.academic.service;

import edu.jecrc.portal.modules.academic.dto.AcademicBatchDto;
import edu.jecrc.portal.modules.academic.dto.AcademicProgramDto;
import edu.jecrc.portal.modules.academic.dto.DepartmentDto;
import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.academic.repository.AcademicBatchRepository;
import edu.jecrc.portal.modules.academic.repository.AcademicProgramRepository;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicMasterService {

    private final DepartmentRepository departmentRepository;
    private final AcademicProgramRepository programRepository;
    private final AcademicBatchRepository batchRepository;

    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AcademicProgramDto> getAllPrograms(Integer departmentId) {
        List<AcademicProgram> programs = (departmentId != null)
                ? programRepository.findByDepartmentId(departmentId)
                : programRepository.findAll();

        return programs.stream()
                .map(AcademicProgramDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AcademicBatchDto> getAllBatches(Integer programId) {
        List<AcademicBatch> batches = (programId != null)
                ? batchRepository.findByProgramId(programId)
                : batchRepository.findAll();

        return batches.stream()
                .map(AcademicBatchDto::fromEntity)
                .collect(Collectors.toList());
    }
}
