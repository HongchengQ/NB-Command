# NB Command

[EN](../README.md) | [中文](README_CN.md) | [日本語](README_JA.md) | [한국어](README_KO.md)

## 소개

NB Command는 [Nebula](https://github.com/Melledy/Nebula)용으로 특별히 설계된 그래픽 원격 명령 실행 도구입니다.

## 기능

- 다국어 지원: 중국어, 영어, 일본어, 한국어 지원
- 게임 관리: 플레이어 관리, 아이템 관리, 캐릭터 관리 등 제공
- 그래픽 인터페이스: 직관적인 운영 인터페이스로 복잡한 명령 실행 과정을 단순화
- 구성 저장: 서버 주소와 인증 토큰 자동 저장
- 명령 기록: 실행된 명령 기록을 저장하여 추적 및 재사용 용이
- 데이터 핸드북: 내장형 캐릭터 및 아이템 데이터 핸드북으로 빠른 검색과 선택 지원

## 설치 및 실행

1. 최신 릴리즈에서 해당 플랫폼의 실행 가능 패키지를 다운로드하세요 [releases](https://github.com/HongchengQ/NB-Command/releases)
2. 압축을 풀고 실행 파일을 실행하세요
> 어떤 nbcommand-windows.zip을 다운로드해야 할지 모르겠다면 이것이 가장 좋은 선택입니다

또는 소스에서 빌드:
- Java 21
- maven

```bash
# 애플리케이션 실행
mvn javafx:run
```

```bash
# 실행 파일 빌드
mvn package
```
출력 디렉토리는 `/target/nbcommand`에 있습니다

## 사용법

1. 상단에 서버 주소와 인증 토큰을 입력하세요
2. 왼쪽 패널에서 명령 카테고리를 선택하세요
3. 중간 목록에서 특정 명령을 선택하세요
4. 필요에 따라 명령 매개변수를 입력하세요
5. 생성된 명령을 미리보고 올바른 것을 확인한 후 실행하세요

### 토큰 획득 방법
> Nebula > `config.json` > `remoteCommand` > `useRemoteServices`가 `true`인지 확인하세요

#### 관리자 권한
> Nebula > `config.json` > `remoteCommand` > `serverAdminKey`를 토큰으로 사용

#### 사용자 권한
> 게임 내에서 명령어 `!remote`를 사용하면 화면에 토큰이 팝업됩니다

## 라이선스

이 프로젝트는 MIT 라이선스에 따라 라이선스가 부여됩니다 - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.