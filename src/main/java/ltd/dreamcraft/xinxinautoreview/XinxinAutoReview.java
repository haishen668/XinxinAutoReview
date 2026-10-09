package ltd.dreamcraft.xinxinautoreview;
import com.xinxin.BotApi.BotAction;
import ltd.dreamcraft.xinxinautoreview.listeners.OnGroupMessage;
import ltd.dreamcraft.xinxinautoreview.listeners.OnPrivateMessage;
import ltd.dreamcraft.xinxinautoreview.listeners.OnUserRequestJoin;
import ltd.dreamcraft.xinxinautoreview.utils.MessageUtil;
import ltd.dreamcraft.xinxinautoreview.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class XinxinAutoReview extends JavaPlugin {
    public static XinxinAutoReview instance;

    public static XinxinAutoReview getInstance() {
        return instance;
    }

    public static Map<String, List<String>> categories = new HashMap<>();
    List<Map<?, ?>> categoryConfig = getConfig().getMapList("Settings.VerificationMessageCategories");
    public static Map<String, Integer> matchedCategoriesCount = new HashMap<>(); // 用于跟踪匹配的类别数量

    @Override
    public void onEnable() {
        instance = this;
        Metrics metrics = new Metrics(this,21873);
        // Plugin startup logic
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new OnGroupMessage(), this);
        getServer().getPluginManager().registerEvents(new OnPrivateMessage(), this);
        if (getConfig().getBoolean("Settings.AutoAgreeFun")) {
            getServer().getPluginManager().registerEvents(new OnUserRequestJoin(), this);
        }
        //初始化
        for (Map<?, ?> category : categoryConfig) {
            String categoryName = (String) category.get("name");
            List<String> keywords = (List<String>) category.get("keywords");
            categories.put(categoryName, keywords);
        }
        Map<String, Integer> savedCategoryCounts = loadCategoryCounts(XinxinAutoReview.getInstance().getDataFolder() + "\\category_counts.txt");
        for (Map.Entry<String, Integer> entry : savedCategoryCounts.entrySet()) {
            matchedCategoriesCount.put(entry.getKey(), matchedCategoriesCount.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        matchedCategoriesCount.clear();
        categories.clear();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("AutoReview.admin")) {
            if (args[0].equalsIgnoreCase("draw")) {
                Bukkit.getScheduler().runTask(this, () -> {
                    String message = MessageUtil.bufferedImgToMsg(generatePieChart(matchedCategoriesCount));
                    BotAction.sendPrivateMessage(Long.parseLong(getConfig().getString("Settings.admin")), message, true);
                });
                sender.sendMessage("§a已经成功绘制饼图");
                return true;
            }
        }
        return false;
    }

    public Map<String, Integer> loadCategoryCounts(String filename) {
        Map<String, Integer> categoryCounts = new HashMap<>();
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile(); // 如果文件不存在，创建一个新文件
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String categoryName = parts[0];
                        int count = Integer.parseInt(parts[1]);
                        categoryCounts.put(categoryName, count);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categoryCounts;
    }

    public static BufferedImage generatePieChart(Map<String, Integer> categoryCounts) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        // 将类别数量添加到数据集
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createRingChart(
                "平台来源统计图", // 饼图标题
                dataset, // 数据集
                true, // 是否显示图例
                true, // 是否生成工具提示
                true // 是否生成URL链接
        );
        Font font = new Font("SimSun", Font.PLAIN, 20);
        chart.getTitle().setFont(font);
        LegendTitle legend = chart.getLegend(0);
        legend.setItemFont(font);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setLabelFont(font);
        plot.setSectionDepth(0.3);

        // 配置图形外观
        plot.setSectionDepth(0.35);
        // 显示标签
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}:{1} ({2})"));
        plot.setLabelLinkStyle(PieLabelLinkStyle.STANDARD); // 使用折线
        plot.setLabelOutlinePaint(null);
        plot.setLabelBackgroundPaint(Color.white);

        // 配置绘图区域
        plot.setSectionDepth(0.35);
        plot.setSeparatorsVisible(false);
        plot.setOutlinePaint(null);

        // 生成饼图图像
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();


        chart.draw(g2, new Rectangle(width, height));

        return image;
    }
}