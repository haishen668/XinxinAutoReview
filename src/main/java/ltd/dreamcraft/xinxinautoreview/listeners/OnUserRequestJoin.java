package ltd.dreamcraft.xinxinautoreview.listeners;

import com.xinxin.BotApi.BotAction;
import com.xinxin.BotEvent.GroupRequestEvent;
import ltd.dreamcraft.xinxinautoreview.XinxinAutoReview;
import ltd.dreamcraft.xinxinautoreview.utils.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author haishen668
 */
public class OnUserRequestJoin implements Listener {
    String flag = "";
    @EventHandler
    public void RequestCheck(GroupRequestEvent event) {
        // 防止重复执行 验证消息会有flag 如果是重复的flag就代表是重复的验证消息就不会执行下面的程序
        if (!event.getFlag().equals(flag)) {
            flag = event.getFlag();
        } else {
            return;
        }
        //可能是由于依赖插件的问题 需要过滤一下具体的事件
        if (!"add".equals(event.getSub_type())) {
            return;
        }
        FileConfiguration config = XinxinAutoReview.getInstance().getConfig();
        List<String> groupList = config.getStringList("Settings.GroupList");

        if (groupList.contains(String.valueOf(event.getGroup_id()))) {
            //等级验证 1.0.1 功能添加
            int levelLimit = config.getInt("Settings.level_limit_min",0);
            if (levelLimit != 0){
                int level = getQQLevel(event);
                if (level == -1) {
                    System.out.println("功能失效请联系作者");
                    return;
                }
                if (level < levelLimit) {
                    event.setGroupRequest(false, config.getString("Settings.AutoRefuseMessage"));
                    if (config.getBoolean("Settings.MessagePush")) {
                        BotAction.sendPrivateMessage(Long.parseLong(config.getString("Settings.admin")), config.getString("Settings.AutoRefuseMessagePrivate")
                                .replace("{qq}", String.valueOf(event.getUser_id()))
                                .replace("{message}", event.getComment()));
                    }
                    return;
                }
            }

            //qq用户发送的群验证消息
            String verifyMessage = event.getComment();
            //标记是否通过验证
            boolean isApprove = false;
            outerLoop: // 外层循环标签
            for (Map.Entry<String, List<String>> entry : XinxinAutoReview.categories.entrySet()) {
                String categoryName = entry.getKey();
                List<String> keywords = entry.getValue();
                for (String keyword : keywords) {
                    //全部转换为小写
                    if (verifyMessage.toLowerCase().contains(keyword.toLowerCase())) {
                            // 设置验证消息为通过
                            event.setGroupRequest(true, "");
                            // 发送私聊消息告诉 管理员(如果消息推送开启)
                            if (config.getBoolean("Settings.MessagePush")) {
                                BotAction.sendPrivateMessage(config.getLong("Settings.admin"),
                                        XinxinAutoReview.getInstance().getConfig().getString("Settings.AutoAgreedMessagePrivate")
                                                .replace("{qq}", String.valueOf(event.getUser_id()))
                                                .replace("{message}", event.getComment()));
                            }
                            // 使匹配的类别数量+1
                            XinxinAutoReview.matchedCategoriesCount.put(categoryName, XinxinAutoReview.matchedCategoriesCount.getOrDefault(categoryName, 0) + 1);
                            //标记通过验证
                            isApprove = true;
                            //跳出两层for内层循环至循环标签 否则会处理多条群信息如果内容是mcbbs bbs 这种雷同的东西
                            break outerLoop;

                    }else {
                        if (keyword.startsWith("[regex]")) {
                            int answerIndex = verifyMessage.indexOf("答案：");
                            if (answerIndex != -1) {
                                verifyMessage = verifyMessage.substring(answerIndex + 3); // 截取"答案："之后的内容
                            }
                            // 解析正则表达式
                            String regex = keyword.substring(7).trim(); // 去掉前缀"[regex]"
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(verifyMessage);
                            if (matcher.find()) {
                                // 设置验证消息为通过
                                event.setGroupRequest(true, "");
                                // 发送私聊消息告诉 管理员(如果消息推送开启)
                                if (config.getBoolean("Settings.MessagePush")) {
                                    BotAction.sendPrivateMessage(config.getLong("Settings.admin"),
                                            XinxinAutoReview.getInstance().getConfig().getString("Settings.AutoAgreedMessagePrivate")
                                                    .replace("{qq}", String.valueOf(event.getUser_id()))
                                                    .replace("{message}", event.getComment()));
                                }
                                // 使匹配的类别数量+1
                                XinxinAutoReview.matchedCategoriesCount.put(categoryName, XinxinAutoReview.matchedCategoriesCount.getOrDefault(categoryName, 0) + 1);
                                //标记通过验证
                                isApprove = true;
                                //跳出两层for内层循环至循环标签 否则会处理多条群信息如果内容是mcbbs bbs 这种雷同的东西
                                break outerLoop;
                            }
                        }
                    }
                }
            }
            //保存统计信息到文本中
            saveCategoryCounts(XinxinAutoReview.matchedCategoriesCount, XinxinAutoReview.getInstance().getDataFolder() + "\\category_counts.txt");


            if (config.getBoolean("Settings.MessagePush")) {
                //若消息推送开启,发送统计信息图表给管理员
                String imgBase64Str = MessageUtil.bufferedImgToMsg(XinxinAutoReview.generatePieChart(XinxinAutoReview.matchedCategoriesCount));
                BotAction.sendPrivateMessage(config.getLong("Settings.admin"), imgBase64Str);
            }

            //拒绝加群申请
            if (config.getBoolean("Settings.AutoRefuseFun") && !isApprove) {
                event.setGroupRequest(false, config.getString("Settings.AutoRefuseMessage"));
                if (config.getBoolean("Settings.MessagePush")) {
                    BotAction.sendPrivateMessage(Long.parseLong(config.getString("Settings.admin")), config.getString("Settings.AutoRefuseMessagePrivate")
                            .replace("{qq}", String.valueOf(event.getUser_id()))
                            .replace("{message}", event.getComment()));
                }
            }

        }
    }

    /**
     * 将类别计数保存到文件
     *
     * @param categoryCounts
     * @param filename
     */
    public void saveCategoryCounts(Map<String, Integer> categoryCounts, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int getQQLevel(GroupRequestEvent event) {
        long qq = event.getUser_id();
        String urlString = "https://api.52hyjs.com/api/level?qq=" + qq;
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("HTTP GET Request Failed with Error code : " + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                conn.disconnect();

                JSONObject json = new JSONObject(sb.toString());

                if (json.has("data")) {
                    JSONObject dataObject = json.getJSONObject("data");
                    if (dataObject != null && dataObject.has("iQQLevel")) {
                        return dataObject.getInt("iQQLevel");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (attempt == maxRetries) {
                    return -1;  // 返回-1表示查询失败
                }
            }
        }

        return -1;
    }
}


