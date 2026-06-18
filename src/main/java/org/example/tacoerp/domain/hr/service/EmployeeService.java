package org.example.tacoerp.domain.hr.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.Employee;
import org.example.tacoerp.domain.hr.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Flux<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Flux<Employee> findByName(String name) {
        return employeeRepository.findByNameContaining(name);
    }

    public Flux<Employee> findByStatus(String status) {
        return employeeRepository.findByStatus(status);
    }

    public Mono<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Mono<Employee> findByUserId(Long userId) {
        return employeeRepository.findByUserId(userId);
    }

    public Mono<Employee> create(Employee employee) {
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        return employeeRepository.save(employee);
    }

    public Mono<Employee> update(Long id, Employee updated) {
        return employeeRepository.findById(id)
                .flatMap(emp -> {
                    emp.setName(updated.getName());
                    emp.setEmployeeNo(updated.getEmployeeNo());
                    emp.setBirthDate(updated.getBirthDate());
                    emp.setGender(updated.getGender());
                    emp.setAddress(updated.getAddress());
                    emp.setDepartmentId(updated.getDepartmentId());
                    emp.setPositionId(updated.getPositionId());
                    emp.setHireDate(updated.getHireDate());
                    emp.setResignDate(updated.getResignDate());
                    emp.setStatus(updated.getStatus());
                    emp.setUpdatedAt(LocalDateTime.now());
                    return employeeRepository.save(emp);
                });
    }

    public Mono<Void> delete(Long id) {
        return employeeRepository.deleteById(id);
    }
}
