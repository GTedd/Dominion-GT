package cn.lunadeer.dominion.managers;

import cn.lunadeer.dominion.Dominion;
import cn.lunadeer.dominion.dtos.Flag;
import cn.lunadeer.minecraftpluginutils.VaultConnect.VaultConnect;
import cn.lunadeer.minecraftpluginutils.XLogger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    public static ConfigManager instance;

    public ConfigManager(Dominion plugin) {
        instance = this;
        new Translation(plugin);
        _plugin = plugin;
        _plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        _plugin.reloadConfig();
        _file = _plugin.getConfig();
        _language = _file.getString("Language", "zh-cn");
        Translation.instance.loadLocale(_language);
        _debug = _file.getBoolean("Debug", false);
        _timer = _file.getBoolean("Timer", false);
        XLogger.setDebug(_debug);
        _db_type = _file.getString("Database.Type", "sqlite");
        _db_host = _file.getString("Database.Host", "localhost");
        _db_port = _file.getString("Database.Port", "5432");
        _db_name = _file.getString("Database.Name", "dominion");
        _db_user = _file.getString("Database.User", "postgres");
        _db_pass = _file.getString("Database.Pass", "postgres");
        _auto_create_radius = _file.getInt("AutoCreateRadius", 10);
        _spawn_protection = _file.getInt("Limit.SpawnProtection", 10);
        _blue_map = _file.getBoolean("BlueMap", false);
        _dynmap = _file.getBoolean("Dynmap", false);
        _auto_clean_after_days = _file.getInt("AutoCleanAfterDays", 180);
        _limit_op_bypass = _file.getBoolean("Limit.OpByPass", true);
        _check_update = _file.getBoolean("CheckUpdate", true);
        _tp_enable = _file.getBoolean("Teleport.Enable", false);
        _tp_delay = _file.getInt("Teleport.Delay", 0);
        _tp_cool_down = _file.getInt("Teleport.CoolDown", 0);
        _tool = _file.getString("Tool", "ARROW");

        _economy_enable = _file.getBoolean("Economy.Enable", false);
        if (getEconomyEnable()) {
            new VaultConnect(this._plugin);
        }
        _fly_permission_nodes = _file.getStringList("FlyPermissionNodes");
        _residence_migration = _file.getBoolean("ResidenceMigration", false);
        _group_title_enable = _file.getBoolean("GroupTitle.Enable", false);
        _group_title_prefix = _file.getString("GroupTitle.Prefix", "&#ffffff[");
        _group_title_suffix = _file.getString("GroupTitle.Suffix", "&#ffffff]");

        GroupLimit defaultGroup = new GroupLimit();
        defaultGroup.setLimitSizeX(_file.getInt("Limit.SizeX", 128), null);
        defaultGroup.setLimitSizeY(_file.getInt("Limit.SizeY", 64), null);
        defaultGroup.setLimitSizeZ(_file.getInt("Limit.SizeZ", 128), null);
        defaultGroup.setLimitMinY(_file.getInt("Limit.MinY", -64), null);
        defaultGroup.setLimitMaxY(_file.getInt("Limit.MaxY", 320), null);
        defaultGroup.setLimitAmount(_file.getInt("Limit.Amount", 10), null);
        defaultGroup.setLimitDepth(_file.getInt("Limit.Depth", 3), null);
        defaultGroup.setLimitVert(_file.getBoolean("Limit.Vert", false), null);
        defaultGroup.setPrice(_file.getDouble("Economy.Price", 10.0));
        defaultGroup.setPriceOnlyXZ(_file.getBoolean("Economy.OnlyXZ", false));
        defaultGroup.setRefundRatio(_file.getDouble("Economy.Refund", 0.85));
        ConfigurationSection worldSettings = _file.getConfigurationSection("Limit.WorldSettings");
        if (worldSettings != null) {
            defaultGroup.addWorldLimits(WorldSetting.load("config.yml", worldSettings));
        }
        groupLimits.put("default", defaultGroup);

        groupLimits.putAll(GroupLimit.loadGroups(_plugin));

        checkRules();
        saveAll();  // 回写文件 防止文件中的数据不完整
        Flag.loadFromFile();    // 加载 Flag 配置
    }

    public void saveAll() {
        // 删除旧文件
        new File(_plugin.getDataFolder(), "config.yml").delete();
        // 保存新文件
        _plugin.saveDefaultConfig();
        // 重新加载
        _plugin.reloadConfig();
        _file = _plugin.getConfig();

        // 保存配置
        _file.set("Database.Type", _db_type);
        _file.set("Database.Host", _db_host);
        _file.set("Database.Port", _db_port);
        _file.set("Database.Name", _db_name);
        _file.set("Database.User", _db_user);
        _file.set("Database.Pass", _db_pass);

        _file.set("Language", _language);
        _file.setComments("Language", List.of(Translation.Config_Comment_Language.trans()));

        _file.set("AutoCreateRadius", _auto_create_radius);
        _file.setComments("AutoCreateRadius", Arrays.asList(Translation.Config_Comment_AutoCreateRadius.trans(), Translation.Config_Comment_NegativeOneDisabled.trans()));

        _file.setComments("Limit", List.of(Translation.Config_Comment_DefaultLimit.trans()));
        _file.set("Limit.SpawnProtection", _spawn_protection);
        _file.setInlineComments("Limit.SpawnProtection", List.of(Translation.Config_Comment_SpawnProtectRadius.trans() + Translation.Config_Comment_NegativeOneDisabled.trans()));
        _file.set("Limit.MinY", groupLimits.get("default").getLimitMinY(null));
        _file.setInlineComments("Limit.MinY", List.of(Translation.Config_Comment_MinY.trans()));
        _file.set("Limit.MaxY", groupLimits.get("default").getLimitMaxY(null));
        _file.setInlineComments("Limit.MaxY", List.of(Translation.Config_Comment_MaxY.trans()));
        _file.set("Limit.SizeX", groupLimits.get("default").getLimitSizeX(null));
        _file.setInlineComments("Limit.SizeX", List.of(Translation.Config_Comment_SizeX.trans() + Translation.Config_Comment_NegativeOneUnlimited.trans()));
        _file.set("Limit.SizeY", groupLimits.get("default").getLimitSizeY(null));
        _file.setInlineComments("Limit.SizeY", List.of(Translation.Config_Comment_SizeY.trans() + Translation.Config_Comment_NegativeOneUnlimited.trans()));
        _file.set("Limit.SizeZ", groupLimits.get("default").getLimitSizeZ(null));
        _file.setInlineComments("Limit.SizeZ", List.of(Translation.Config_Comment_SizeZ.trans() + Translation.Config_Comment_NegativeOneUnlimited.trans()));
        _file.set("Limit.Amount", groupLimits.get("default").getLimitAmount(null));
        _file.setInlineComments("Limit.Amount", List.of(Translation.Config_Comment_Amount.trans() + Translation.Config_Comment_NegativeOneUnlimited.trans()));
        _file.set("Limit.Depth", groupLimits.get("default").getLimitDepth(null));
        _file.setInlineComments("Limit.Depth", List.of(Translation.Config_Comment_Depth.trans() + Translation.Config_Comment_ZeroDisabled.trans() + Translation.Config_Comment_NegativeOneUnlimited.trans()));
        _file.set("Limit.Vert", groupLimits.get("default").getLimitVert(null));
        _file.setInlineComments("Limit.Vert", List.of(Translation.Config_Comment_Vert.trans()));
        _file.set("Limit.OpByPass", _limit_op_bypass);
        _file.setInlineComments("Limit.OpByPass", List.of(Translation.Config_Comment_OpBypass.trans()));
        _file.set("Limit.WorldSettings", groupLimits.get("default").getWorldSettings());
        _file.setInlineComments("Limit.WorldSettings", List.of(Translation.Config_Comment_WorldSettings.trans()));

        _file.set("Teleport.Enable", _tp_enable);
        _file.set("Teleport.Delay", _tp_delay);
        _file.setInlineComments("Teleport.Delay", List.of(Translation.Config_Comment_TpDelay.trans()));
        _file.set("Teleport.CoolDown", _tp_cool_down);
        _file.setInlineComments("Teleport.CoolDown", List.of(Translation.Config_Comment_TpCoolDown.trans()));

        _file.set("AutoCleanAfterDays", _auto_clean_after_days);
        _file.setComments("AutoCleanAfterDays", Arrays.asList(Translation.Config_Comment_AutoCleanAfterDays.trans(), Translation.Config_Comment_NegativeOneDisabled.trans()));

        _file.set("Tool", _tool);
        _file.setComments("Tool", List.of(Translation.Config_Comment_ToolName.trans()));

        _file.setComments("Economy", Arrays.asList(Translation.Config_Comment_Economy.trans(), Translation.Config_Comment_VaultRequired.trans()));
        _file.set("Economy.Enable", _economy_enable);
        _file.set("Economy.Price", groupLimits.get("default").getPrice());
        _file.setInlineComments("Economy.Price", List.of(Translation.Config_Comment_Price.trans()));
        _file.set("Economy.OnlyXZ", groupLimits.get("default").getPriceOnlyXZ());
        _file.setInlineComments("Economy.OnlyXZ", List.of(Translation.Config_Comment_OnlyXZ.trans()));
        _file.set("Economy.Refund", groupLimits.get("default").getRefundRatio());
        _file.setInlineComments("Economy.Refund", List.of(Translation.Config_Comment_Refund.trans()));

        _file.set("FlyPermissionNodes", _fly_permission_nodes);
        _file.setComments("FlyPermissionNodes", List.of(Translation.Config_Comment_FlyPermission.trans()));

        _file.set("ResidenceMigration", _residence_migration);
        _file.setComments("ResidenceMigration", List.of(Translation.Config_Comment_ResidenceMigration.trans()));

        _file.setComments("GroupTitle", Arrays.asList(
                Translation.Config_Comment_GroupTitle.trans(),
                Translation.Config_Comment_GroupTitleVariable.trans(),
                Translation.Config_Comment_GroupTitleColor.trans()));
        _file.set("GroupTitle.Enable", _group_title_enable);
        _file.set("GroupTitle.Prefix", _group_title_prefix);
        _file.set("GroupTitle.Suffix", _group_title_suffix);

        _file.set("BlueMap", _blue_map);
        _file.set("Dynmap", _dynmap);

        _file.set("CheckUpdate", _check_update);

        _file.set("Debug", _debug);
        _file.set("Timer", _timer);
        _file.setInlineComments("Timer", List.of(Translation.Config_Comment_PerformanceTimer.trans()));

        _plugin.saveConfig();
    }

    public Boolean isDebug() {
        return _debug;
    }

    public void setDebug(Boolean debug) {
        _debug = debug;
        _file.set("Debug", debug);
        _plugin.saveConfig();
        XLogger.setDebug(debug);
    }

    public Boolean TimerEnabled() {
        return _timer;
    }

    public String getDbType() {
        return _db_type;
    }

    public String getDbHost() {
        return _db_host;
    }

    public String getDbPort() {
        return _db_port;
    }

    public String getDbName() {
        return _db_name;
    }

    public void setDbUser(String db_user) {
        _db_user = db_user;
        _file.set("Database.User", db_user);
    }

    public String getDbUser() {
        if (_db_user.contains("@")) {
            setDbUser("'" + _db_user + "'");
        }
        return _db_user;
    }

    public void setDbPass(String db_pass) {
        _db_pass = db_pass;
        _file.set("Database.Pass", db_pass);
    }

    public String getDbPass() {
        return _db_pass;
    }

    public Integer getLimitSizeX(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitSizeX(player.getWorld());
    }

    public Integer getLimitSizeY(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitSizeY(player.getWorld());
    }

    public Integer getLimitSizeZ(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitSizeZ(player.getWorld());
    }

    public Integer getAutoCreateRadius() {
        return _auto_create_radius;
    }

    public void setAutoCreateRadius(Integer radius) {
        _auto_create_radius = radius;
        _file.set("AutoCreateRadius", radius);
    }

    public Boolean getBlueMap() {
        return _blue_map;
    }

    public Boolean getDynmap() {
        return _dynmap;
    }

    public Integer getAutoCleanAfterDays() {
        return _auto_clean_after_days;
    }

    public void setAutoCleanAfterDays(Integer auto_clean_after_days) {
        _auto_clean_after_days = auto_clean_after_days;
        _file.set("AutoCleanAfterDays", auto_clean_after_days);
    }

    public Integer getLimitMinY(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitMinY(player.getWorld());
    }

    public Integer getLimitMaxY(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitMaxY(player.getWorld());
    }

    public Integer getLimitAmount(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitAmount(player.getWorld());
    }

    public Integer getLimitDepth(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitDepth(player.getWorld());
    }

    public Boolean getLimitVert(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getLimitVert(player.getWorld());
    }

    public List<String> getWorldBlackList(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getWorldBlackList();
    }

    public Boolean getLimitOpBypass() {
        return _limit_op_bypass;
    }

    public Boolean getCheckUpdate() {
        return _check_update;
    }

    public Boolean getTpEnable() {
        return _tp_enable;
    }

    public Integer getTpDelay() {
        return _tp_delay;
    }

    public void setTpDelay(Integer tp_delay) {
        _tp_delay = tp_delay;
        _file.set("Teleport.Delay", tp_delay);
    }

    public Integer getTpCoolDown() {
        return _tp_cool_down;
    }

    public void setTpCoolDown(Integer tp_cool_down) {
        _tp_cool_down = tp_cool_down;
        _file.set("Teleport.CoolDown", tp_cool_down);
    }

    public Material getTool() {
        return Material.getMaterial(_tool);
    }

    public void setTool(String tool) {
        _tool = tool;
        _file.set("Tool", tool);
    }

    public Boolean getEconomyEnable() {
        return _economy_enable;
    }

    public Float getEconomyPrice(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getPrice().floatValue();
    }

    public Boolean getEconomyOnlyXZ(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getPriceOnlyXZ();
    }

    public Float getEconomyRefund(Player player) {
        return groupLimits.get(getPlayerGroup(player)).getRefundRatio().floatValue();
    }

    public List<String> getFlyPermissionNodes() {
        return _fly_permission_nodes;
    }

    public Boolean getResidenceMigration() {
        return _residence_migration;
    }

    public Integer getSpawnProtection() {
        return _spawn_protection;
    }

    public Boolean getGroupTitleEnable() {
        return _group_title_enable;
    }

    public void setGroupTitleEnable(Boolean group_title_enable) {
        _group_title_enable = group_title_enable;
        _file.set("GroupTitle.Enable", group_title_enable);
    }

    public String getGroupTitlePrefix() {
        return _group_title_prefix;
    }

    public String getGroupTitleSuffix() {
        return _group_title_suffix;
    }

    public String getLanguage() {
        return _language;
    }

    public void checkRules() {
        if (Material.getMaterial(_tool) == null) {
            XLogger.err(Translation.Config_Check_ToolNameError);
            setTool("ARROW");
        }
        if (getAutoCreateRadius() == 0) {
            XLogger.err(Translation.Config_Check_AutoCreateRadiusError);
            setAutoCreateRadius(10);
        }
        if (getAutoCleanAfterDays() == 0) {
            XLogger.err(Translation.Config_Check_AutoCleanAfterDaysError);
            setAutoCleanAfterDays(180);
        }
        if (getTpDelay() < 0) {
            XLogger.err(Translation.Config_Check_TpDelayError);
            setTpDelay(0);
        }
        if (getTpCoolDown() < 0) {
            XLogger.err(Translation.Config_Check_TpCoolDownError);
            setTpCoolDown(0);
        }

        for (GroupLimit limit : groupLimits.values()) {
            limit.checkRules();
        }
    }

    private final Dominion _plugin;
    private FileConfiguration _file;
    private Boolean _debug;
    private Boolean _timer;

    private String _db_type;
    private String _db_host;
    private String _db_port;
    private String _db_user;
    private String _db_pass;
    private String _db_name;

    private String _language;

    private Integer _auto_create_radius;

    private Boolean _limit_op_bypass;

    private Boolean _blue_map;
    private Boolean _dynmap;
    private Integer _auto_clean_after_days;

    private Boolean _check_update;

    private Boolean _tp_enable;
    private Integer _tp_delay;
    private Integer _tp_cool_down;
    private String _tool;

    private Boolean _economy_enable;

    private List<String> _fly_permission_nodes;
    private Boolean _residence_migration;
    private Integer _spawn_protection;

    private Boolean _group_title_enable;
    private String _group_title_prefix;
    private String _group_title_suffix;

    private final Map<String, GroupLimit> groupLimits = new HashMap<>();

    private String getPlayerGroup(@Nullable Player player) {
        if (player == null) {
            return "default";
        }
        for (String group : groupLimits.keySet()) {
            if (group.equals("default")) {
                continue;
            }
            if (player.hasPermission("group." + group)) {
                return group;
            }
        }
        return "default";
    }
}
