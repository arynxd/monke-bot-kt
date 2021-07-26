package me.arynxd.monke.objects.jda

import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.managers.AudioManager
import net.dv8tion.jda.api.managers.DirectAudioController
import net.dv8tion.jda.api.managers.Presence
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction
import net.dv8tion.jda.api.requests.restaction.CommandEditAction
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.api.requests.restaction.GuildAction
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.utils.cache.CacheView
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService

class NoOpJDA : JDA {
    private val unsupported = "This operation is not supported"

    override fun getStatus(): JDA.Status {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getGatewayIntents(): EnumSet<GatewayIntent> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getCacheFlags(): EnumSet<CacheFlag> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun unloadUser(userId: Long): Boolean {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getGatewayPing(): Long {
        throw UnsupportedOperationException(unsupported)
    }

    override fun awaitStatus(status: JDA.Status, vararg failOn: JDA.Status?): JDA {
        throw UnsupportedOperationException(unsupported)
    }

    override fun cancelRequests(): Int {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getRateLimitPool(): ScheduledExecutorService {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getGatewayPool(): ScheduledExecutorService {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getCallbackPool(): ExecutorService {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getHttpClient(): OkHttpClient {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getDirectAudioController(): DirectAudioController {
        throw UnsupportedOperationException(unsupported)
    }

    override fun setEventManager(manager: IEventManager?) {
        throw UnsupportedOperationException(unsupported)
    }

    override fun addEventListener(vararg listeners: Any?) {
        throw UnsupportedOperationException(unsupported)
    }

    override fun removeEventListener(vararg listeners: Any?) {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getRegisteredListeners(): MutableList<Any> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun retrieveCommands(): RestAction<MutableList<Command>> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun retrieveCommandById(id: String): RestAction<Command> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun upsertCommand(command: CommandData): CommandCreateAction {
        throw UnsupportedOperationException(unsupported)
    }

    override fun updateCommands(): CommandListUpdateAction {
        throw UnsupportedOperationException(unsupported)
    }

    override fun editCommandById(id: String): CommandEditAction {
        throw UnsupportedOperationException(unsupported)
    }

    override fun deleteCommandById(commandId: String): RestAction<Void> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun createGuild(name: String): GuildAction {
        throw UnsupportedOperationException(unsupported)
    }

    override fun createGuildFromTemplate(code: String, name: String, icon: Icon?): RestAction<Void> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getAudioManagerCache(): CacheView<AudioManager> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getUserCache(): SnowflakeCacheView<User> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getMutualGuilds(vararg users: User?): MutableList<Guild> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getMutualGuilds(users: MutableCollection<User>): MutableList<Guild> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun retrieveUserById(id: Long, update: Boolean): RestAction<User> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getGuildCache(): SnowflakeCacheView<Guild> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getUnavailableGuilds(): MutableSet<String> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun isUnavailable(guildId: Long): Boolean {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getRoleCache(): SnowflakeCacheView<Role> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getCategoryCache(): SnowflakeCacheView<Category> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getStoreChannelCache(): SnowflakeCacheView<StoreChannel> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getTextChannelCache(): SnowflakeCacheView<TextChannel> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getVoiceChannelCache(): SnowflakeCacheView<VoiceChannel> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getPrivateChannelCache(): SnowflakeCacheView<PrivateChannel> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun openPrivateChannelById(userId: Long): RestAction<PrivateChannel> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getEmoteCache(): SnowflakeCacheView<Emote> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getEventManager(): IEventManager {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getSelfUser(): SelfUser {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getPresence(): Presence {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getShardInfo(): JDA.ShardInfo {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getToken(): String {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getResponseTotal(): Long {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getMaxReconnectDelay(): Int {
        throw UnsupportedOperationException(unsupported)
    }

    override fun setAutoReconnect(reconnect: Boolean) {
        throw UnsupportedOperationException(unsupported)
    }

    override fun setRequestTimeoutRetry(retryOnTimeout: Boolean) {
        throw UnsupportedOperationException(unsupported)
    }

    override fun isAutoReconnect(): Boolean {
        throw UnsupportedOperationException(unsupported)
    }

    override fun isBulkDeleteSplittingEnabled(): Boolean {
        throw UnsupportedOperationException(unsupported)
    }

    override fun shutdown() {
        throw UnsupportedOperationException(unsupported)
    }

    override fun shutdownNow() {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getAccountType(): AccountType {
        throw UnsupportedOperationException(unsupported)
    }

    override fun retrieveApplicationInfo(): RestAction<ApplicationInfo> {
        throw UnsupportedOperationException(unsupported)
    }

    override fun setRequiredScopes(scopes: MutableCollection<String>): JDA {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getInviteUrl(vararg permissions: Permission?): String {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getInviteUrl(permissions: MutableCollection<Permission>?): String {
        throw UnsupportedOperationException(unsupported)
    }

    override fun getShardManager(): ShardManager? {
        throw UnsupportedOperationException(unsupported)
    }

    override fun retrieveWebhookById(webhookId: String): RestAction<Webhook> {
        throw UnsupportedOperationException(unsupported)
    }
}