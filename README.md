NutriMate API 🥗
1. 🎯 프로젝트 소개 및 핵심 기능
NutriMate API는 사용자의 건강 목표, 알레르기, 선호 식단(비건, 키토 등)을 기반으로 개인화된 식단 추천 및 영양소 섭취 분석 기능을 제공하는 고성능 백엔드 API입니다. 건강에 관심이 많은 사용자들이 자신의 영양 상태를 손쉽게 관리할 수 있도록 돕는 것을 목표로 합니다.

핵심 기능:

사용자 프로필 관리: 개인의 신체 정보, 건강 목표, 알레르기 및 식단 선호도를 설정하고 관리합니다.
AI 기반 식단 추천: 사용자 프로필에 최적화된 일일 식단(아침/점심/저녁)을 추천받을 수 있습니다.
실시간 영양소 분석: 섭취한 음식을 기록하고 일일 목표 대비 칼로리, 탄수화물, 단백질, 지방 섭취량을 분석합니다.




2. 🗄️ 핵심 데이터베이스 스키마 (PostgreSQL)
주요 엔티티 간의 관계를 간략하게 표현한 스키마입니다.

-- 사용자 정보
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    height_cm NUMERIC(5, 2),
    weight_kg NUMERIC(5, 2),
    target_calories INT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 사용자 선호 및 제한 사항
CREATE TABLE user_preferences (
    preference_id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(user_id),
    preference_type VARCHAR(20), -- 'ALLERGY', 'DIET_TYPE'
    preference_value VARCHAR(100) -- 'Nuts', 'Gluten', 'Vegan', 'Keto'
);

-- 음식/식재료 정보
CREATE TABLE foods (
    food_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    calories_per_100g INT,
    carbs_g NUMERIC(5, 2),
    protein_g NUMERIC(5, 2),
    fat_g NUMERIC(5, 2)
);

-- 추천된 식단 계획
CREATE TABLE meal_plans (
    plan_id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(user_id),
    plan_date DATE NOT NULL,
    meal_type VARCHAR(10), -- 'Breakfast', 'Lunch', 'Dinner'
    food_id INT REFERENCES foods(food_id),
    serving_size_g INT
);

-- 사용자의 일일 영양 섭취 분석 결과
CREATE TABLE nutritional_analysis (
    analysis_id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(user_id),
    analysis_date DATE NOT NULL,
    total_calories INT,
    total_carbs_g NUMERIC(7, 2),
    total_protein_g NUMERIC(7, 2),
    total_fat_g NUMERIC(7, 2)
);



3. 🛠️ 기술 스택 및 구현 상세 전략
언어 및 프레임워크: Java 17, Spring Boot 3
데이터베이스: PostgreSQL
캐싱/세션: Redis
인증: JWT (JSON Web Token) 기반
JWT 인증 및 보안
인증 흐름: 사용자가 로그인하면 서버는 Access Token(유효기간: 15분)과 Refresh Token(유효기간: 7일)을 발급합니다.
토큰 검증: API Gateway(또는 Spring Security Filter) 레벨에서 모든 요청의 Authorization 헤더에 담긴 Access Token을 검증합니다. 서명 키를 사용하여 토큰의 유효성을 검증하고, 만료 시 401 Unauthorized 에러를 반환합니다. 클라이언트는 이때 Refresh Token을 사용하여 새로운 Access Token을 재발급받습니다.
Spring Security 구성: SecurityFilterChain Bean을 설정하여 특정 엔드포인트(예: /api/auth/**)는 모두에게 허용하고, 나머지 API(예: /api/v1/**)는 isAuthenticated() 조건을 만족하는, 즉 유효한 JWT 토큰을 소유한 사용자만 접근 가능하도록 제어합니다.
Redis 캐싱 전략
목표: 자주 조회되지만 데이터 변경이 적은 식재료별 영양소 정보(foods 테이블) 조회 시 DB 부하를 줄여 API 응답 속도를 향상시킵니다.
구현: Spring의 @Cacheable 어노테이션을 활용합니다. FoodService의 getFoodById(food_id) 메소드에 @Cacheable(value = "foods", key = "#food_id")를 적용합니다.
캐시 키: foods::[food_id] 형태로 Redis에 저장됩니다.
캐시 만료 정책: TTL(Time-To-Live)을 24시간으로 설정합니다. 영양소 정보는 거의 변하지 않으므로 긴 만료 시간을 설정하고, 만약 데이터 수정이 발생하면 해당 캐시를 명시적으로 제거(evict)하는 로직을 추가합니다.




5. 🐳 로컬 환경 구축 (Docker)
프로젝트 루트 디렉터리에서 아래 명령어를 실행하여 API 서버, PostgreSQL, Redis 컨테이너를 한 번에 실행할 수 있습니다.

docker-compose up -d
docker-compose.yml 예시
