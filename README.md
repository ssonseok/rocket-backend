# EMS 기반 IoT 장비 관리 시스템 (OpenEMS Reference)

**백엔드(Spring Boot) + 엣지(라즈베리파이) + UI(HTML·CSS·JS)**  
**Java 17 / Spring Boot / MySQL**  
프론트 레포지토리: https://github.com/ssonseok/rocket-frontend  
ppt : [에너지 관리 시스템a.pptx](https://github.com/user-attachments/files/24071083/a.pptx)

---

## 📌 프로젝트 소개

이 프로젝트는 **OpenEMS 오픈소스 구조를 참고하여 개발한 EMS(Energy Management System) 기반 장비 관리 프로그램**입니다.  
전체 시스템은 **Backend / Edge / UI**의 3계층 구조로 나누어져 있으며, 각 계층이 REST API와 WebSocket 기반으로 통신합니다.

---

## 🧩 시스템 구성

### 1️⃣ Backend (Spring Boot)
- Java 17  
- Spring Boot 기반 REST API 서버  
- 장비 등록, 상태 조회, 데이터 수신 기능 구현  
- MySQL과 연동해 장비/엣지/측정값 저장
- 로그인한 계정의 이메일로 경고 메일 전송  
- OpenEMS 구조를 참고하여 관리 서버 역할 수행  
- JSON 기반 API  
- WebSocket 기반 실시간 데이터 스트림 처리  

### 2️⃣ Edge (Raspberry Pi + JAR)
- 라즈베리파이에서 Java JAR로 구동  
- 장비 센싱 데이터 감지 → Backend로 실시간 전송  
- WebSocket 연결로 데이터 스트림 송신  
- Edge별 ID 관리  
- 네트워크 끊김/재연결 로직 포함

### 3️⃣ UI (Frontend)
- **HTML + CSS + JavaScript**  
- Fetch API로 백엔드 REST 호출  
- 실시간 상태 업데이트 화면 제공  
- 장비 목록 / 등록 / 수정 / 삭제 등 관리 페이지 구성  

---

## 📁 주요 기능

### ✔ 장비 등록
- 시리얼 번호 기반 등록  
- Edge와 연결 확인 후 최종 등록 처리  

### ✔ 장비 상태 조회
- Backend ↔ Edge WebSocket 연결 상태 표시  
- 장비별 실시간 데이터 스트림 표시  

### ✔ 장비 관리 기능
- 장비 목록 조회  
- 장비 정보 수정  
- 장비 삭제  

### ✔ 데이터 저장
- 실시간 수집값을 MySQL에 저장  
- 장비 / Edge / 측정값 각각 테이블 관리

---
