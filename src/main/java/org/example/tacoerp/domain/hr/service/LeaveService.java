package org.example.tacoerp.domain.hr.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.AnnualLeave;
import org.example.tacoerp.domain.hr.entity.LeaveRequest;
import org.example.tacoerp.domain.hr.repository.AnnualLeaveRepository;
import org.example.tacoerp.domain.hr.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        return create(request, false, null);
    }

    /**
     * @param autoApprove true면 결재 단계 없이 즉시 APPROVED 처리 (팀장 이상 본인 신청)
     * @param approverId  자동승인 시 결재자로 기록할 사용자(보통 본인) ID. autoApprove가 false면 무시됨
     */
    public Mono<LeaveRequest> create(LeaveRequest request, boolean autoApprove, Long approverId) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일과 종료일을 입력해주세요."));
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다."));
        }

        // 일수는 클라이언트 입력값을 신뢰하지 않고 서버에서 항상 재계산 (양 끝 날짜 포함)
        long dayCount = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        request.setDays(BigDecimal.valueOf(dayCount));

        LocalDateTime now = LocalDateTime.now();
        request.setCreatedAt(now);
        request.setUpdatedAt(now);

        if (autoApprove) {
            request.setStatus("APPROVED");
            request.setApprovedBy(approverId);
            request.setApprovedAt(now);
        } else {
            request.setStatus("PENDING");
        }

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

