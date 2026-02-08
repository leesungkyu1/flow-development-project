1. 프로젝트 개요
   본 프로젝트는 파일 업로드 시 보안 위협이 될 수 있는 특정 확장자를 제한하고 관리하는 관리형 웹 애플리케이션입니다. 시스템이 강제하는 고정 확장자와 운영자가 유연하게 제어하는 커스텀 확장자 이원화 구조를 통해 시스템 보안성과 운영 편의성을 동시에 확보했습니다.

    1.1. 기술 스택 (Tech Stack)
        Frontend: React.js (Single Page Application)

        Backend: Java 17, Spring Boot 3.x, Spring Data JPA

        Database: H2 Database (File Mode)

        Infrastructure: AWS EC2 (t3.micro), Docker, Docker-Compose, Nginx

2. 시스템 아키텍처 및 설계
    2.1. 데이터베이스 구조

    FIXED_EXTENSION {
        bigint id PK
        varchar name UK
        varchar description
        boolean checked
        varchar magic_number
    }
    CUSTOM_EXTENSION {
        bigint id PK
        varchar name UK
    }
    2.2. 테이블 설계 의무 및 확장성
        fixed_extension: 시스템 핵심 보안 규칙을 저장합니다. 특히 magic_number 필드를 설계에 포함하여, 단순 확장자 변조(예: .exe를 .txt로 변경)를 바이너리 수준에서 탐지할 수 있는 확장성을 확보했습니다.
        custom_extension: 최대 200개까지 수용 가능한 사용자 정의 규칙을 저장하며, 고정 확장자와의 논리적 무결성을 비즈니스 로직에서 보장합니다.

3. 핵심 기능 및 기술적 고려 사항
    3.1. 다중 계층 유효성 검증 (Multi-layer Validation)
        데이터 무결성과 시스템 안정성을 위해 클라이언트와 서버 양단에서 철저한 검증 로직을 구현했습니다.
        
        중복 및 개수 제한: 커스텀 확장자 추가 시 서버 사이드에서 count()를 통한 200개 제한 준수 여부와 고정/커스텀 통합 중복 체크를 수행합니다.
        
        글자 수 및 특수 문자 제어: 정규 표현식을 활용하여 알파벳과 숫자로 구성을 제한하고, DB 스키마에 맞춘 길이 제한(10~20자)을 통해 예외 발생 가능성을 원천 차단했습니다.

    3.2. 보안 고도화: 매직 넘버(Magic Number) 활용 설계
        단순한 확장자 필터링의 한계를 극복하기 위해 파일 시그니처 검증을 위한 기반을 마련했습니다.
        
        변조 방지 전략: 사용자가 악의적으로 실행 파일(.exe)의 이름만 변경하여 업로드하는 경우를 대비해, DB에 저장된 고정 확장자별 매직 넘버(예: MZ 시그니처 등)와 파일의 실제 바이너리 데이터를 대조하는 검증 프로세스를 설계 단계에서 고려했습니다. 이를 통해 향후 실제 파일 업로드 기능 구현 시 강력한 보안 필터를 즉시 적용할 수 있습니다.
    
    3.3. 저사양(t3.micro) 배포 환경 최적화
        RAM 1GB의 제한적인 환경에서 안정적인 서비스를 제공하기 위해 Docker 빌드 및 운영 전략을 최적화했습니다.

        멀티 스테이지 빌드 (Multi-stage Build):
            빌드 단계(Gradle/Node.js)와 실행 단계(JRE/Nginx)를 분리하여 최종 이미지 용량을 70% 이상 절감했습니다.
            이를 통해 배포 시 네트워크 비용을 줄이고 EC2의 디스크 자원을 효율적으로 관리합니다.

        리소스 한계 극복:
            Swap Memory (2GB): 물리 메모리 부족으로 인한 OOM(Out of Memory) 현상을 방지하기 위해 스왑 파일을 구성했습니다.
            JVM 힙 제한: -Xmx512M 설정을 통해 애플리케이션이 가용한 리소스 내에서 안정적으로 동작하도록 제어했습니다.

4. 보안 가중 (Security Hardening) 및 인프라 설정
    4.1. 네트워크 보안 강화
        Nginx Reverse Proxy: WAS 포트(8080)를 외부에 노출하지 않고 Nginx(80)를 프록시로 사용하여 서버 정보를 은닉했습니다.
        Rate Limiting: 동일 IP로부터 발생하는 무분별한 요청을 Nginx 단에서 차단(limit_req)하여 서비스 가용성을 확보했습니다.
        AWS Security Group: SSH(22번) 포트는 관리자 IP만 허용하는 화이트리스트 방식을 채택하여 공격 표면(Attack Surface)을 최소화했습니다.

    4.2. 중앙 집중식 예외 처리 및 로깅
        @RestControllerAdvice를 통한 전역 예외 처리로 일관된 에러 응답 규격을 유지하며, 시스템 내부 스택 트레이스가 외부에 노출되지 않도록 보안성을 강화했습니다.

5. 향후 개선 방향
    5.1. 캐싱 전략 (Caching) 도입
        고정 확장자와 같이 변동성이 낮고 조회가 빈번한 데이터에 대해 Caffeine Cache를 활용한 로컬 캐싱을 적용하여 DB I/O 부하를 줄일 계획입니다.

    5.2. 데이터 영속성 고도화
        현재 H2 파일 모드에서 운영용 Managed DB인 AWS RDS로 전환하여 데이터 안정성과 백업 체계를 구축할 예정입니다.

6. 결론 (Interview Point)
   "본 프로젝트는 단순한 CRUD 기능을 넘어, 매직 넘버를 통한 보안 확장성과 저사양 클라우드 환경에서의 Docker 최적화를 실무적으로 해결한 경험입니다. 특히 Nginx를 활용한 인프라 보안과 멀티 스테이지 빌드 전략은 제한된 자원 내에서 안정적인 서비스를 운영하기 위한 엔지니어링적 고민의 결과입니다."