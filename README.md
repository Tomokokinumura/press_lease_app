# Press Lease App

## 📝 概要

Press Lease App は、アパレルブランド等でプレス活動を行う際に、リース伝票の作成・管理・データ連携を行うためのWebアプリケーションです。
Spring Boot をベースに構築されており、Google Sheets API と連携してデータ管理を行います。

---

## 🚀 特徴

* プレスリリースの作成・編集
* Web画面からのデータ管理（Thymeleaf）
* MyBatisによるDB操作
* PostgreSQL対応
* Google Sheets API連携
* Dockerによる環境構築

---

## 🛠️ 使用技術

### バックエンド

* Java 17
* Spring Boot 3.x
* Spring MVC

### フロントエンド

* Thymeleaf

### データベース

* PostgreSQL

### ORM / データアクセス

* MyBatis

### 外部連携

* Google Sheets API

### その他

* Docker
* Lombok

---

## 💻 セットアップ

### 1. クローン

```bash
git clone https://github.com/Tomokokinumura/press_lease_app.git
cd press_lease_app
```

---

### 2. 環境変数設定

以下の環境変数を設定してください

```
DB_URL=jdbc:postgresql://localhost:5432/your_db
DB_USER=your_user
DB_PASSWORD=your_password

GOOGLE_CREDENTIALS=path/to/credentials.json
```

---

### 3. アプリ起動

#### Mavenの場合

```bash
./mvnw spring-boot:run
```

#### Gradleの場合

```bash
./gradlew bootRun
```

---

### 4. Dockerで起動（任意）

```bash
docker build -t press-lease-app .
docker run -p 8080:8080 press-lease-app
```

---

## 📁 ディレクトリ構成（例）

```
src/
 ├── main/
 │   ├── java/
 │   ├── resources/
 │   │   ├── templates/
 │   │   └── application.yml
 └── test/
```

---

## 🔑 主な依存関係

* Spring Boot Web
* Thymeleaf
* MyBatis
* PostgreSQL Driver
* Google API Client
* Apache POI

---

## 🧪 テスト

```bash
./mvnw test
```

---

## 📌 今後の改善

* UI/UX改善
* APIのREST化
* 認証機能の強化
* バリデーション追加

---

## 👤 作者

* Tomoko Kinumura

---

## 📄 ライセンス

MIT
