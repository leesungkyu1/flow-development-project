DELETE FROM fixed_extension; -- 기존 데이터 삭제

INSERT INTO fixed_extension (name, description, checked, magic_number) VALUES ('bat', 'Windows 명령줄 실행용 배치 파일', false, '406563686F');
INSERT INTO fixed_extension (name, description, checked, magic_number) VALUES ('exe', '프로그램 실행용 윈도우 실행 파일', false, '4D5A');
INSERT INTO fixed_extension (name, description, checked, magic_number) VALUES ('sh', 'Unix/Linux 시스템 실행용 쉘 스크립트', false, '2321');
INSERT INTO fixed_extension (name, description, checked, magic_number) VALUES ('zip', '데이터 압축 및 아카이브용 파일', false, '504B0304');
INSERT INTO fixed_extension (name, description, checked, magic_number) VALUES ('cks', '체크섬 또는 특정 시스템용 설정 파일', false, '');
INSERT INTO fixed_extension (name, description, checked, magic_number) VALUES ('etc', '기타 시스템 관리용 확장자', false, '');
INSERT INTO fixed_extension (name, description, checked, magic_number) VALUES ('scr', '윈도우 화면 보호기 또는 스크립트 파일', false, '4D5A');