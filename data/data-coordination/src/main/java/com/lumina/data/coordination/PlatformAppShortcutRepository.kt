package com.lumina.data.coordination

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.os.UserManager
import com.lumina.core.common.IoDispatcher
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppInfo
import com.lumina.core.model.AppShortcut
import com.lumina.domain.apps.AppShortcutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

private const val MAX_SHORTCUTS = 4

@Singleton
class PlatformAppShortcutRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userManager: UserManager,
    private val logger: Logger
) : AppShortcutRepository{
    private val TAG = this::class.java.simpleName
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    override suspend fun getShortcuts(app: AppInfo): List<AppShortcut> = withContext(ioDispatcher) {
        try {
            val userHandle = userManager.getUserForSerialNumber(app.userHandleNumber)
                ?: return@withContext emptyList()

            val query = ShortcutQuery().apply {
                setQueryFlags(
                    ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    ShortcutQuery.FLAG_MATCH_MANIFEST or
                    ShortcutQuery.FLAG_MATCH_PINNED
                )
                setPackage(app.packageName)
            }

            val shortcuts = launcherApps.getShortcuts(query, userHandle)
            shortcuts?.map { info ->
                AppShortcut(
                    shortcutId = info.id,
                    app = app,
                    shortLabel = info.shortLabel?.toString() ?: app.displayName,
                    longLabel = info.longLabel?.toString() ?: app.displayName,
                    rank = info.rank
                )
            }
            ?.sortedBy { it.rank }
            ?.take(MAX_SHORTCUTS)
            ?: emptyList()
        } catch(e: SecurityException) {
            logger.w(TAG, "To view shortcuts, this needs to be the default launcher.")
            emptyList()
        } catch (e: Exception) {
            logger.e(TAG, "Failed to get shortcuts for ${app.packageName}.", e)
            emptyList()
        }
    }
}
