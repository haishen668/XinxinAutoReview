
# 使用方法
请在config.yml中配置管理员QQ和需要监听的群聊列表即可使用，请认真查看配置项 进行插件的配置

# 功能展示与详解

## 默认配置效果

![](https://image.dreamcraft.ltd/LightPicture/2024/04/f038b43a2b51abcd.png)
## 重点配置项详解
> 在`VerificationMessageCategories:`下添加或修改类别和关键字。例如，如果你想添加一个名为"哔哩哔哩"的类别，包含关键字"bilibili"和"b站"，可以这样在下面添加，这样操作后监听到验证消息中有bilibili或者b站的字眼就会自动通过审核，然后将其统计到哔哩哔哩这个类别当中去，后面会以图表的形式展示。


```typescript
VerificationMessageCategories:
  - name: "哔哩哔哩"
    keywords:
      - "bilibili"
      - "b站"
```
> 进阶玩法：关键词你可以使用正则表达式,keywords需要以[regex]开头即可
例子如下  配置的效果是只要关键词包含“b站”就能通过审核
> {.is-info}
{.is-warning}
```typescript
VerificationMessageCategories:
  - name: "哔哩哔哩"
    keywords:
      - "[regex] .*b站.*"
```


> 在群内与机器人的私聊消息中监听关键词“成员统计” 关键词可自行修改
> 如果触发关键词就机器人就会给你发送一张饼状统计图 具体配置效果图如下

**配置项如下：**
```typescript
Setting:
	GroupKeywordListen: "成员统计"
```
**效果图如下：**
![](https://image.dreamcraft.ltd/LightPicture/2024/04/e462bc05f416c399.png)

