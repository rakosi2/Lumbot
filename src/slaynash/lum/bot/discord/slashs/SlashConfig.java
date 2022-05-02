package slaynash.lum.bot.discord.slashs;

import java.time.Duration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import slaynash.lum.bot.DBConnectionManagerLum;
import slaynash.lum.bot.discord.GuildConfiguration;
import slaynash.lum.bot.discord.Moderation;
import slaynash.lum.bot.utils.ExceptionUtils;

public class SlashConfig {

    public void sendReply(SlashCommandEvent event, String guildID) {
        try {
            if (!guildID.matches("^\\d{18,19}$")) {
                event.reply("Invalid Guild ID. Please make sure that you are using the digit ID. https://support.discord.com/hc/en-us/articles/206346498").setEphemeral(true).queue();
                return;
            }
            Guild guild = event.getJDA().getGuildById(guildID);
            if (guild == null) {
                event.reply("Guild was not found.").queue();
                return;
            }
            if (Moderation.getAdmins(guild).contains(event.getUser().getIdLong())) {
                GuildConfiguration guildconfig = DBConnectionManagerLum.getGuildConfig(guildID);
                System.out.println("Sent config for " + guild.getName());
                event.reply("Server Config for " + guild.getName() + ": " + guildID)
                    .addActionRow(// Buttons can be in a 5x5
                        guildconfig.ScamShield() ? Button.success("ss", "Scam Shield") : Button.danger("ss", "Scam Shield"),
                        guildconfig.ScamShieldBan() ? Button.danger("ssban", "Scam Shield Ban") : Button.success("ssban", "Scam Shield Kick"),
                        guildconfig.ScamShieldCross() ? Button.success("sscross", "Scam Shield Cross " + (guildconfig.ScamShieldBan() ? "Ban" : "Kick")) : Button.danger("sscross", "Scam Shield Cross " + (guildconfig.ScamShieldBan() ? "Ban" : "Kick")))
                    .addActionRow(
                        guildconfig.DLLRemover() ? Button.success("dll", "DLL Remover") : Button.danger("dll", "DLL Remover"),
                        guildconfig.MLPartialRemover() ? Button.success("partial", "Partial Log remover") : Button.danger("partial", "Partial Log remover"),
                        guildconfig.MLGeneralRemover() ? Button.success("general", "General Log remover") : Button.danger("general", "General Log remover"))
                    .addActionRow(
                        guildconfig.MLLogScan() ? Button.success("log", "MelonLoader Log scanner") : Button.danger("log", "MelonLoader Log scanner"),
                        guildconfig.MLReplies() ? Button.success("mlr", "MelonLoader AutoReplies") : Button.danger("mlr", "MelonLoader AutoReplies"),
                        guildconfig.MLLogReaction() ? Button.success("reaction", "Log Reactions") : Button.danger("reaction", "Log Reactions"))
                    .addActionRow(
                        guildconfig.LumReplies() ? Button.success("thanks", "Chatty Lum") : Button.danger("thanks", "Chatty Lum"),
                        guildconfig.DadJokes() ? Button.success("dad", "Dad Jokes") : Button.danger("dad", "Dad Jokes"))
                    .addActionRow(
                        Button.danger("delete", "Delete this message")).queue();
            }
            else event.reply("You do not have permissions to use this command for the guild " + guild.getName()).setEphemeral(true).queue();
        }
        catch (Exception e) {
            ExceptionUtils.reportException("An error has occurred while sending Slash Reply:", e);
        }
    }

    public void buttonClick(ButtonClickEvent event) {
        try {
            String[] message = event.getMessage().getContentRaw().split(": ");
            if (message.length < 2) {
                event.reply("Can not find Guild ID. Please resend the config command.").setEphemeral(true).queue();
                return;
            }
            String guildID = message[message.length - 1];
            Guild guild = event.getJDA().getGuildById(guildID);
            if (Moderation.getAdmins(guild).contains(event.getUser().getIdLong())) {
                GuildConfiguration guildconfig = DBConnectionManagerLum.getGuildConfig(guildID);
                switch (event.getComponentId()) {
                    case "ss" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.SCAMSHIELD.string, !guildconfig.ScamShield());
                        event.editButton(!guildconfig.ScamShield() ? Button.success("ss", "Scam Shield") : Button.danger("ss", "Scam Shield")).queue();
                        checkBanPerm(event, guild, guildconfig.ScamShieldBan());
                    }
                    case "dll" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.DLLREMOVER.string, !guildconfig.DLLRemover());
                        event.editButton(!guildconfig.DLLRemover() ? Button.success("dll", "DLL Remover") : Button.danger("dll", "DLL Remover")).queue();
                        checkDllRemovePerm(event, guild);
                    }
                    case "reaction" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.LOGREACTION.string, !guildconfig.MLLogReaction());
                        event.editButton(!guildconfig.MLLogReaction() ? Button.success("reaction", "Log Reactions") : Button.danger("reaction", "Log Reactions")).queue();
                    }
                    case "thanks" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.LUMREPLIES.string, !guildconfig.LumReplies());
                        event.editButton(!guildconfig.LumReplies() ? Button.success("thanks", "Chatty Lum") : Button.danger("thanks", "Chatty Lum")).queue();
                    }
                    case "dad" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.DADJOKES.string, !guildconfig.DadJokes());
                        event.editButton(!guildconfig.DadJokes() ? Button.success("dad", "Dad Jokes") : Button.danger("dad", "Dad Jokes")).queue();
                    }
                    case "partial" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.PARTIALLOGREMOVER.string, !guildconfig.MLPartialRemover());
                        event.editButton(!guildconfig.MLPartialRemover() ? Button.success("partial", "Partial Log remover") : Button.danger("partial", "Partial Log remover")).queue();
                    }
                    case "general" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.GENERALLOGREMOVER.string, !guildconfig.MLGeneralRemover());
                        event.editButton(!guildconfig.MLGeneralRemover() ? Button.success("general", "General Log remover") : Button.danger("general", "General Log remover")).queue();
                    }
                    case "log" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.LOGSCAN.string, !guildconfig.MLLogScan());
                        event.editButton(!guildconfig.MLLogScan() ? Button.success("log", "MelonLoader Log scanner") : Button.danger("log", "MelonLoader Log scanner")).queue();
                    }
                    case "mlr" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.MLREPLIES.string, !guildconfig.MLReplies());
                        event.editButton(!guildconfig.MLReplies() ? Button.success("mlr", "MelonLoader AutoReplies") : Button.danger("mlr", "MelonLoader AutoReplies")).queue();
                    }
                    case "ssban" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.SSBAN.string, !guildconfig.ScamShieldBan());
                        event.editButton(!guildconfig.ScamShieldBan() ? Button.danger("ssban", "Scam Shield Ban") : Button.success("ssban", "Scam Shield Kick")).queue();
                        event.editButton(!guildconfig.ScamShieldCross() ? Button.success("sscross", "Scam Shield Cross " + (!guildconfig.ScamShieldBan() ? "Ban" : "Kick")) : Button.danger("sscross", "Scam Shield Cross " + (!guildconfig.ScamShieldBan() ? "Ban" : "Kick"))).queue();
                        checkBanPerm(event, guild, !guildconfig.ScamShieldBan());
                    }
                    case "sscross" -> {
                        DBConnectionManagerLum.setGuildSetting(guildID, GuildConfiguration.Setting.SSCROSS.string, !guildconfig.ScamShieldCross());
                        event.editButton(!guildconfig.ScamShieldCross() ? Button.success("sscross", "Scam Shield Cross " + (!guildconfig.ScamShieldBan() ? "Ban" : "Kick")) : Button.danger("sscross", "Scam Shield Cross " + (!guildconfig.ScamShieldBan() ? "Ban" : "Kick"))).queue();
                    }
                    case "delete" -> event.getMessage().delete().queue();
                    default -> {
                    }
                }
            }
            else {
                event.reply("You do not have permissions to edit settings for " + guild.getName()).setEphemeral(true).queue();
            }
        }
        catch (Exception e) {
            ExceptionUtils.reportException("An error has occurred while updating buttons:", event.getChannel().getName(), e);
        }
    }

    private void checkBanPerm(ButtonClickEvent event, Guild guild, boolean ban) {
        String message = "";
        if (ban) {
            if (!guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
                if (guild.getSelfMember().hasPermission(Permission.KICK_MEMBERS))
                    message = "I don't have ban permission but I will kick scammers instead.";
                else
                    message = "I don't have ban permission so I can't remove scammers should they appear.";
            }
        }
        else {
            if (!guild.getSelfMember().hasPermission(Permission.KICK_MEMBERS))
                message = "I don't have kick permission so I can't remove scammers should they appear.";
            if (!guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) && !guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS))
                message = "I can't clean up messages after I removed the scammer. I need either Manage Messages or Ban permission.";
        }
        if (message.isEmpty())
            return;
        final String finalMessage = message;
        if (event.getChannelType() == ChannelType.PRIVATE || !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
            event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(finalMessage)).delay(Duration.ofSeconds(60)).flatMap(Message::delete).queue();
        }
        else {
            event.getTextChannel().sendMessage(finalMessage).delay(Duration.ofSeconds(30)).flatMap(Message::delete).queue();
        }
    }
    private void checkDllRemovePerm(ButtonClickEvent event, Guild guild) {
        if (!guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            final String message = "I don't have manage message permission so I can't remove dll and zip files.";
            if (event.getChannelType() == ChannelType.PRIVATE || !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
                event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).delay(Duration.ofSeconds(60)).flatMap(Message::delete).queue();
            }
            else {
                event.getTextChannel().sendMessage(message).delay(Duration.ofSeconds(30)).flatMap(Message::delete).queue();
            }
        }
    }
}
