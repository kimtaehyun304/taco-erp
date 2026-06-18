package org.example.tacoerp.domain.hr.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.Salary;
import org.example.tacoerp.domain.hr.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryRepository salaryRepository;

    public Flux<Salary> findAll() {
        return salaryRepository.findAll();
    }

    public Flux<Salary> findByEmployee(Long employeeId) {
        return salaryRepository.findByEmployeeIdOrderByPayMonthDesc(employeeId);
    }

    public Mono<Salary> findById(Long id) {
        return salaryRepository.findById(id);
    }

    public Mono<Salary> create(Salary salary) {
        BigDecimal net = salary.getBaseSalary()
                .add(salary.getBonus() != null ? salary.getBonus() : BigDecimal.ZERO)
                .subtract(salary.getDeduction() != null ? salary.getDeduction() : BigDecimal.ZERO);
        salary.setNetSalary(net);
        salary.setCreatedAt(LocalDateTime.now());
        return salaryRepository.save(salary);
    }

    public Mono<Salary> update(Long id, Salary updated) {
        return salaryRepository.findById(id)
                .flatMap(sal -> {
                    sal.setBaseSalary(updated.getBaseSalary());
                    sal.setBonus(updated.getBonus());
                    sal.setDeduction(updated.getDeduction());
                    BigDecimal net = sal.getBaseSalary()
                            .add(sal.getBonus() != null ? sal.getBonus() : BigDecimal.ZERO)
                            .subtract(sal.getDeduction() != null ? sal.getDeduction() : BigDecimal.ZERO);
                    sal.setNetSalary(net);
                    sal.setPayMonth(updated.getPayMonth());
                    sal.setPaidAt(updated.getPaidAt());
                    return salaryRepository.save(sal);
                });
    }

    public Mono<Void> delete(Long id) {
        return salaryRepository.deleteById(id);
    }
}
