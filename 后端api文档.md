# 后端api文档

## Prize

### 奖品列表
端点 : /api/prizes
方法 : GET
请求参数 : 无
响应 :
[
  {
    "id": 1,示例奖品",
    "rarity": 3,
    "fiveStarType": null,
    "description": "示例描述",
    "isRepeatable": true,
    "enabled": true
  }
]

### 抽奖
端点：/api/draw
方法： GET
请求参数： 无
描述： 根据概率和保底规则，返回抽奖结果
响应：
{
  "prizeId": 1,
  "prizeName": "示例奖品",
  "rarity": 3,
  "fiveStarType": null,
  "description": "示例描述",
  "isRepeatable": true,
  "enabled": true
}
错误响应：
{
  "error": "请确保每种稀有度的奖品数量不为0"
}
{
  "error": "计划点不足“
}