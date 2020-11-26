# Rankpoint
---
### 소개
* [Paper](https://papermc.io) 기반의 플러그인입니다.
* 일정 포인트를 얻어 랭크를 올립니다.
---
### 적용법
1. 플러그인을 서버에 적용하고 서버를 킵니다.
2. 플러그인 폴더의 config 파일과 message 파일을 자신에 맞추어 수정합니다.
(config 의 group은 펄미션의 그룹 이름을 적습니다.)
3. rankpoint reload 명령어를 사용합니다.
---
### 명령어
    /rankpoint me 현재 자신이 가지고 있는 포인트를 표시합니다.
    /rankpoint look <name> : 주어진 플레이어의 포인트를 조회합니다.
    /rankpoint give <name> <points> : 주어진 플레이어에게 포인트룰 추가합니다.
    /rankpoint giveall <points> : 모든 온라인 플레이어에게 포인트를 추가합니다.
    /rankpoint take <name> <points> : 주어진 플레이어의 포인트를 차감합니다.
    /rankpoint set <name> <points> : 주어진 플레이어의 포인트를 설정합니다.
    /rankpoint reset <name> : 주어진 플레이어의 포인트를 0으로 재설정합니다.
    /rankpoint reload : 구성을 다시 로드합니다.
---
### 의존 플러그인
* [Vault](https://dev.bukkit.org/projects/vault)
* 펄미션 플러그인 (ex. Luckperms)
* [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (선택)
---