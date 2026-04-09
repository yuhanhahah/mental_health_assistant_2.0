# API Key 配置说明

## 问题说明

如果遇到 `HTTP 401 - "Invalid token"` 错误，说明 SiliconFlow API Key 未正确配置。

## 解决方案

### 方法一：使用环境变量（推荐）

#### Windows (PowerShell)
```powershell
$env:SPRING_AI_OPENAI_API_KEY="你的实际API密钥"
```

#### Windows (CMD)
```cmd
set SPRING_AI_OPENAI_API_KEY=你的实际API密钥
```

#### Linux/Mac
```bash
export SPRING_AI_OPENAI_API_KEY="你的实际API密钥"
```

### 方法二：直接修改配置文件（不推荐用于生产环境）

编辑 `src/main/resources/application.yml` 文件，将：
```yaml
api-key: ${SPRING_AI_OPENAI_API_KEY:your-api-key}
```
改为：
```yaml
api-key: 你的实际API密钥
```

### 方法三：使用 application-local.yml（推荐用于本地开发）

1. 创建 `src/main/resources/application-local.yml` 文件
2. 添加以下内容：
```yaml
spring:
  ai:
    openai:
      api-key: 你的实际API密钥
```

3. 启动应用时指定 profile：
```bash
java -jar your-app.jar --spring.profiles.active=local
```

或在 IDE 中设置 VM options：
```
-Dspring.profiles.active=local
```

## 获取 SiliconFlow API Key

1. 访问 [SiliconFlow 官网](https://siliconflow.cn/)
2. 注册/登录账号
3. 进入控制台，获取 API Key
4. 将 API Key 配置到环境变量或配置文件中

## 注意事项

⚠️ **重要**：
- 不要将真实的 API Key 提交到 Git 仓库
- 建议将 `application-local.yml` 添加到 `.gitignore`
- 生产环境应使用环境变量或密钥管理服务（如 AWS Secrets Manager、Azure Key Vault 等）


