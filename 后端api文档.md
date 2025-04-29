# 后端api文档

## Prize

### 奖品列表
端点：GET /api/prizes  
请求参数：无  
响应示例：
```json
[
  {
    "id": 1,
    "name": "示例奖品",
    "rarity": 3,
    "fiveStarType": null,
    "description": "示例描述",
    "isRepeatable": true,
    "enabled": true
  }
]
```

### 抽奖
端点：GET /api/prizes/draw  
描述：根据概率和保底规则，返回抽奖结果  
响应示例：
```json
{
  "id": 5,
  "name": "五星限量",
  "rarity": 5,
  "fiveStarType": "limited",
  "description": "稀有奖励",
  "isRepeatable": false,
  "enabled": true
}
```
错误响应示例（计划点不足）：
```json
{
  "message": "计划点不足，无法抽奖",
  "errorType": "INSUFFICIENT_POINTS",
  "status": "error"
}
```
错误响应示例（无可用奖品）：
```json
{
  "message": "当前奖池中没有可用奖品",
  "errorType": "NO_PRIZE_AVAILABLE",
  "status": "error"
}
```

### 添加奖品
端点：POST /api/prizes  
请求体示例：
```json
{
  "name": "新奖品",
  "rarity": 4,
  "fiveStarType": null,
  "description": "测试添加",
  "isRepeatable": true,
  "enabled": true
}
```
成功响应示例：
```json
{
  "id": 10,
  "name": "新奖品",
  "rarity": 4,
  "fiveStarType": null,
  "description": "测试添加",
  "isRepeatable": true,
  "enabled": true
}
```
参数校验失败示例：
```json
{
  "message": "奖品名称不能为空",
  "errorType": "VALIDATION_ERROR",
  "status": "error"
}
```

### 删除奖品
端点：DELETE /api/prizes/{id}  
示例：DELETE /api/prizes/10  
成功响应示例：
```json
{
  "message": "Prize deleted successfully",
  "status": "success"
}
```
失败响应示例：
```json
{
  "message": "Prize not found",
  "status": "error"
}
```

### 更新奖品
端点：PUT /api/prizes/{id}  
请求体示例：
```json
{
  "name": "更新后名称",
  "rarity": 5,
  "fiveStarType": "normal",
  "description": "更新描述",
  "isRepeatable": false
}
```
成功响应示例：
```json
{
  "id": 10,
  "name": "更新后名称",
  "rarity": 5,
  "fiveStarType": "normal",
  "description": "更新描述",
  "isRepeatable": false,
  "enabled": true
}
```

### 切换奖品状态
端点：PUT /api/prizes/{id}/toggle  
请求体示例：
```json
{ "enabled": false }
```
成功响应示例：
```json
{
  "id": 10,
  "name": "更新后名称",
  "enabled": false,
  // ...其他字段...
}
```
失败响应示例：
```json
{
  "message": "切换状态失败原因",
  "status": "error"
}
```