package org.example.tacoerp.domain.hr.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.AnnualLeave;
import org.example.tacoerp.domain.hr.entity.LeaveRequest;
import org.example.tacoerp.domain.hr.repository.AnnualLeaveRepository;
import org.example.tacoerp.domain.hr.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AnnualLeaveRepository annualLeaveRepository;

    public Flux<LeaveRequest> findByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    public Flux<LeaveRequest> findPending() {
        return leaveRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    public Flux<LeaveRequest> findAll() {
        return leaveRequestRepository.findAll();
    }

    public Mono<LeaveRequest> findById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    public Mono<LeaveRequest> create(LeaveRequest request) {
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return leaveRequestRepository.save(request);
    }

    public Mono<LeaveRequest> approve(Long id, Long approverId) {
        return leaveRequestRepository.findById(id)
                .flatMap(req -> {
                    req.setStatus("APPROVED");
                    req.setApprovedBy(approverId);
                    req.setApprovedAt(LocalDateTime.now());
                    req.setUpdatedAt(LocalDateTime.now());
                    return leaveRequestRepository.save(req);
                });
    }

    public Mono<LeaveRequest> reject(Long id) {
        return leaveRequestRepository.findById(id)
                .flatMap(req -> {
                    req.setStatus("REJECTED");
                    req.setUpdatedAt(LocalDateTime.now());
                    return leaveRequestRepository.save(req);
                });
    }

    public Mono<Void> delete(Long id) {
        return leaveRequestRepository.deleteById(id);
    }

    public Mono<AnnualLeave> findAnnualLeave(Long employeeId, int year) {
        return annualLeaveRepository.findByEmployeeIdAndYear(employeeId, year);
    }
}
