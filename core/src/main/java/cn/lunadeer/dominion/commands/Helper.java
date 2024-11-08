package cn.lunadeer.dominion.commands;

import cn.lunadeer.dominion.DominionInterface;
import cn.lunadeer.dominion.api.dtos.*;
import cn.lunadeer.dominion.dtos.PrivilegeTemplateDTO;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.lunadeer.dominion.utils.CommandUtils.playerOnly;

public class Helper {

    public static List<String> dominionFlags() {
        List<Flag> flags = DominionInterface.instance.getEnvironmentFlagsEnabled();
        flags.addAll(DominionInterface.instance.getPrivilegeFlagsEnabled());
        return Arrays.asList(flags.stream().map(Flag::getFlagName).toArray(String[]::new));
    }

    public static List<String> playerPrivileges() {
        List<Flag> flags = DominionInterface.instance.getPrivilegeFlagsEnabled();
        return Arrays.asList(flags.stream().map(Flag::getFlagName).toArray(String[]::new));
    }

    /**
     * 获取玩家可管理的领地列表
     *
     * @param sender 命令发送者
     * @return 领地列表
     */
    public static List<String> playerDominions(CommandSender sender) {
        List<String> dominions_name = new ArrayList<>();
        Player player = playerOnly(sender);
        if (player == null) return dominions_name;
        dominions_name.addAll(playerOwnDominionNames(sender));
        dominions_name.addAll(playerAdminDominionNames(sender));
        return dominions_name;
    }

    public static List<String> dominionGroups(String dominionName) {
        DominionDTO dominion = DominionInterface.instance.getDominion(dominionName);
        if (dominion == null) return new ArrayList<>();
        else return dominion.getGroups().stream().map(GroupDTO::getNamePlain).toList();
    }

    public static List<String> groupPlayers(String domName, String groupName) {
        List<String> players_name = new ArrayList<>();
        DominionDTO dominion = DominionInterface.instance.getDominion(domName);
        if (dominion == null) return players_name;
        GroupDTO group = dominion.getGroups().stream().filter(g -> g.getNamePlain().equals(groupName)).findFirst().orElse(null);
        if (group == null) return players_name;
        for (MemberDTO member : group.getMembers()) {
            PlayerDTO player = DominionInterface.instance.getPlayerDTO(member.getPlayerUUID());
            if (player != null) players_name.add(player.getLastKnownName());
        }
        return players_name;
    }

    public static List<String> playerOwnDominionNames(CommandSender sender) {
        Player player = playerOnly(sender);
        if (player == null) return new ArrayList<>();
        return DominionInterface.instance.getPlayerDominions(player.getUniqueId()).stream().map(DominionDTO::getName).toList();
    }

    public static List<String> playerAdminDominionNames(CommandSender sender) {
        List<String> dominions_name = new ArrayList<>();
        Player player = playerOnly(sender);
        if (player == null) return dominions_name;
        List<MemberDTO> dominions_admin = new ArrayList<>(cn.lunadeer.dominion.dtos.MemberDTO.selectAll(player.getUniqueId()));
        for (MemberDTO member : dominions_admin) {
            if (member.getAdmin()) {
                DominionDTO dom = DominionInterface.instance.getDominion(member.getDomID());
                if (dom == null) continue;
                dominions_name.add(dom.getName());
            }
        }
        return dominions_name;
    }

    public static List<String> allDominions() {
        List<String> dominions_name = new ArrayList<>();
        List<DominionDTO> dominions = DominionInterface.instance.getAllDominions();
        for (DominionDTO dominion : dominions) {
            dominions_name.add(dominion.getName());
        }
        return dominions_name;
    }

    public static List<String> allTemplates(CommandSender sender) {
        List<String> templates_name = new ArrayList<>();
        Player player = playerOnly(sender);
        if (player == null) return templates_name;
        List<PrivilegeTemplateDTO> templates = PrivilegeTemplateDTO.selectAll(player.getUniqueId());
        for (PrivilegeTemplateDTO template : templates) {
            templates_name.add(template.getName());
        }
        return templates_name;
    }

}
