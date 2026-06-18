-- 기본 부서
delete from departments;
INSERT INTO departments (id, name, parent_id, sort_order) VALUES (1, '(주)타코ERP', NULL, 0);
INSERT INTO departments (id, name, parent_id, sort_order) VALUES (2, '경영지원본부', 1, 1);
INSERT INTO departments (id, name, parent_id, sort_order) VALUES (3, '개발본부', 1, 2);
INSERT INTO departments (id, name, parent_id, sort_order) VALUES (4, '인사팀', 2, 1);
INSERT INTO departments (id, name, parent_id, sort_order) VALUES (5, '재무팀', 2, 2);
INSERT INTO departments (id, name, parent_id, sort_order) VALUES (6, '백엔드팀', 3, 1);
INSERT INTO departments (id, name, parent_id, sort_order) VALUES (7, '프론트팀', 3, 2);

-- 직급
delete from positions;
INSERT INTO positions (id, name, level) VALUES (1, '사원', 1);
INSERT INTO positions (id, name, level) VALUES (2, '대리', 2);
INSERT INTO positions (id, name, level) VALUES (3, '과장', 3);
INSERT INTO positions (id, name, level) VALUES (4, '차장', 4);
INSERT INTO positions (id, name, level) VALUES (5, '부장', 5);
INSERT INTO positions (id, name, level) VALUES (6, '이사', 6);
INSERT INTO positions (id, name, level) VALUES (7, '대표이사', 7);

-- 역할
delete from roles;
INSERT INTO roles (id, name, description) VALUES (1, 'ROLE_ADMIN', '시스템 관리자');
INSERT INTO roles (id, name, description) VALUES (2, 'ROLE_HR', '인사 담당자');
INSERT INTO roles (id, name, description) VALUES (3, 'ROLE_MANAGER', '관리자(팀장 이상)');
INSERT INTO roles (id, name, description) VALUES (4, 'ROLE_USER', '일반 사용자');

-- 메뉴
delete from menus;
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (1, '대시보드', '/dashboard', 'bi-speedometer2', NULL, 0);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (2, '사용자/권한', NULL, 'bi-people', NULL, 1);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (3, '사용자 관리', '/admin/users', 'bi-person', 2, 1);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (4, '부서 관리', '/admin/departments', 'bi-diagram-3', 2, 2);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (5, '역할 관리', '/admin/roles', 'bi-shield', 2, 3);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (6, '감사 로그', '/admin/audit-logs', 'bi-journal-text', 2, 4);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (7, '인사(HR)', NULL, 'bi-person-badge', NULL, 2);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (8, '직원 관리', '/hr/employees', 'bi-person-lines-fill', 7, 1);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (9, '근태 관리', '/hr/attendance', 'bi-clock', 7, 2);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (10, '연차/휴가', '/hr/leave', 'bi-calendar3', 7, 3);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (11, '급여 관리', '/hr/salary', 'bi-cash-stack', 7, 4);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (12, '전자결재', NULL, 'bi-file-earmark-check', NULL, 3);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (13, '기안 작성', '/approval/new', 'bi-file-earmark-plus', 12, 1);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (14, '결재함', '/approval/inbox', 'bi-inbox', 12, 2);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (15, '기안함', '/approval/drafts', 'bi-file-earmark', 12, 3);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (16, '게시판', NULL, 'bi-layout-text-window', NULL, 4);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (17, '공지사항', '/board/notice', 'bi-megaphone', 16, 1);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (18, '자료실', '/board/archive', 'bi-folder', 16, 2);
INSERT INTO menus (id, name, url, icon, parent_id, sort_order) VALUES (19, 'FAQ', '/board/faq', 'bi-question-circle', 16, 3);

-- 메뉴 권한
delete from menu_roles;
INSERT INTO menu_roles (menu_id, role_id) VALUES (1, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (1, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (1, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (1, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (2, 1);
INSERT INTO menu_roles (menu_id, role_id) VALUES (3, 1);
INSERT INTO menu_roles (menu_id, role_id) VALUES (4, 1);
INSERT INTO menu_roles (menu_id, role_id) VALUES (5, 1);
INSERT INTO menu_roles (menu_id, role_id) VALUES (6, 1);
INSERT INTO menu_roles (menu_id, role_id) VALUES (7, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (7, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (7, 3);
INSERT INTO menu_roles (menu_id, role_id) VALUES (8, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (8, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (9, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (9, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (9, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (9, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (10, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (10, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (10, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (10, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (11, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (11, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (12, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (12, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (12, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (12, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (13, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (13, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (13, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (13, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (14, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (14, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (14, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (14, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (15, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (15, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (15, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (15, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (16, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (16, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (16, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (16, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (17, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (17, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (17, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (17, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (18, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (18, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (18, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (18, 4);
INSERT INTO menu_roles (menu_id, role_id) VALUES (19, 1); INSERT INTO menu_roles (menu_id, role_id) VALUES (19, 2);
INSERT INTO menu_roles (menu_id, role_id) VALUES (19, 3); INSERT INTO menu_roles (menu_id, role_id) VALUES (19, 4);

-- 기본 사용자 (비밀번호: admin123 -> BCrypt)
delete from users;
INSERT INTO users (id, username, password, name, email, department_id, position_id)
VALUES (1, 'admin', '$2a$10$pxcQ6zVCIGx8M/akyvypr.bPlYR4zdcXNaZBt90j39Ewb6nRQ3IJa', '시스템관리자', 'admin@taco-erp.com', 2, 7);
INSERT INTO users (id, username, password, name, email, department_id, position_id)
VALUES (2, 'hrmanager', '$2a$10$pxcQ6zVCIGx8M/akyvypr.bPlYR4zdcXNaZBt90j39Ewb6nRQ3IJa', '김인사', 'hr@taco-erp.com', 4, 5);
INSERT INTO users (id, username, password, name, email, department_id, position_id)
VALUES (3, 'user1', '$2a$10$pxcQ6zVCIGx8M/akyvypr.bPlYR4zdcXNaZBt90j39Ewb6nRQ3IJa', '홍길동', 'hong@taco-erp.com', 6, 2);

delete from user_roles;
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2); INSERT INTO user_roles (user_id, role_id) VALUES (2, 3);
INSERT INTO user_roles (user_id, role_id) VALUES (3, 4);

-- 기본 직원
delete from employees;
INSERT INTO employees (id, user_id, employee_no, name, hire_date, department_id, position_id, status)
VALUES (1, 1, 'EMP001', '시스템관리자', '2020-01-01', 2, 7, 'ACTIVE');
INSERT INTO employees (id, user_id, employee_no, name, hire_date, department_id, position_id, status)
VALUES (2, 2, 'EMP002', '김인사', '2021-03-01', 4, 5, 'ACTIVE');
INSERT INTO employees (id, user_id, employee_no, name, hire_date, department_id, position_id, status)
VALUES (3, 3, 'EMP003', '홍길동', '2023-06-01', 6, 2, 'ACTIVE');

-- 연차
delete from annual_leaves;
INSERT INTO annual_leaves (employee_id, years, total_days, used_days, remaining_days) VALUES (1, 2026, 15, 3, 12);
INSERT INTO annual_leaves (employee_id, years, total_days, used_days, remaining_days) VALUES (2, 2026, 15, 5, 10);
INSERT INTO annual_leaves (employee_id, years, total_days, used_days, remaining_days) VALUES (3, 2026, 11, 2, 9);

-- 공지사항 샘플
delete from boards;
INSERT INTO boards (id, title, content, board_type, author_id, is_pinned)
VALUES (1, '[공지] 2026년 하계 휴가 안내', '2026년 하계 휴가 기간은 7월 28일(월) ~ 8월 1일(금)입니다.', 'NOTICE', 1, TRUE);
INSERT INTO boards (id, title, content, board_type, author_id, is_pinned)
VALUES (2, '[공지] 보안 패치 안내', '2026년 6월 15일(일) 시스템 보안 패치가 예정되어 있습니다.', 'NOTICE', 1, FALSE);
INSERT INTO boards (id, title, content, board_type, author_id, is_pinned)
VALUES (3, '개발팀 컨벤션 가이드', '코드 컨벤션 가이드 첨부 파일을 확인해주세요.', 'ARCHIVE', 3, FALSE);
INSERT INTO boards (id, title, content, board_type, author_id, is_pinned)
VALUES (4, '연차 신청 방법', '전자결재 > 휴가 신청 메뉴를 통해 연차를 신청할 수 있습니다.', 'FAQ', 2, FALSE);
