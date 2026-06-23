package org.example.tacoerp.domain.hr.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.OvertimeRequest;
import org.example.tacoerp.domain.hr.repository.OvertimeRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class OvertimeService {

    private final OvertimeRequestRepository overtimeRequestRepository;

    public Flux<OvertimeRequest> findByEmployee(Long employeeId) {
        return overtimeRequestRepository.findByEmployeeIdOrderByWorkDateDesc(employeeId);
    }

    public Flux<OvertimeRequest> findPending() {
        return overtimeRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    public Flux<OvertimeRequest> findAll() {
        return overtimeRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public Mono<OvertimeRequest> findById(Long id) {
        return overtimeRequestRepository.findById(id);
    }

    public Mono<OvertimeRequest> create(OvertimeRequest request, boolean autoApprove, Long approverId) {
        if (request.getWorkDate() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "근무일을 입력해주세요."));
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작/종료 시각을 입력해주세요."));
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료 시각은 시작 시각보다 늦어야 합니다."));
        }

        // 시간 계산 (분 단위 → 0.5시간 단위로 반올림)
        long minutes = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
        double rawHours = minutes / 60.0;
        double rounded = Math.round(rawHours * 2) / 2.0; // 0.5 단위
        request.setHours(BigDecimal.valueOf(rounded));

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

        return overtimeRequestRepository.save(request);
    }

    public Mono<OvertimeRequest> approve(Long id, Long approverId) {
        return overtimeRequestRepository.findById(id)
                .flatMap(req -> {
                    req.setStatus("APPROVED");
                    req.setApprovedBy(approverId);
                    req.setApprovedAt(LocalDateTime.now());
                    req.setUpdatedAt(LocalDateTime.now());
                    return overtimeRequestRepository.save(req);
                });
    }

    public Mono<OvertimeRequest> reject(Long id) {
        return overtimeRequestRepository.findById(id)
                .flatMap(req -> {
                    req.setStatus("REJECTED");
                    req.setUpdatedAt(LocalDateTime.now());
                    return overtimeRequestRepository.save(req);
                });
    }

    public Mono<Void> delete(Long id) {
        return overtimeRequestRepository.deleteById(id);
    }
}
