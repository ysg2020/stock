# 주식 배당금 서비스 API
주식 배당금 정보를 확인할수 있는 서비스

[주식 배당금 서비스 API 개발시 얻은 인사이트](https://tobe-lv100.tistory.com/15)

## 기술 스택
- Spring boot , java
- H2, Jpa
- redis , jsoup

## API
- 특정 회사 배당금 조회
- 배당금 검색 - 자동완
- 회사 리스트 조회
- 배당금 저장
- 배당금 삭제
---

### 특정 회사 배당금 조회
- GET /finance/dividend/{companyName}
  - 특정 회사의 배당금을 조회

### 배당금 검색 - 자동완성
- GET /company/autocomplete 
  - 특정 키워드(자동완성)로 시작하는 회사 배당금 검색 
  
### 회사 리스트 조회
- GET /company 
  - 전체 회사 리스트 조회

### 배당금 저장
- POST /company
  - jsoup 라이브러리를 이용해 스크래핑 해온 배당금 데이터를 저장

### 배당금 삭제
- DELETE /company 
  - 특정 회사의 배당금 데이터 삭제

  
