package androidx.media3.demo.session.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

@UnstableApi
class FakeMediaSourceFactory(val context: Context) : MediaSource.Factory {

    private val factory = ProgressiveMediaSource.Factory(OkHttpDataSource.Factory(OkHttpClient()))
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun setDrmSessionManagerProvider(
        drmSessionManagerProvider: DrmSessionManagerProvider
    ): MediaSource.Factory {
        factory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        return this
    }

    override fun setLoadErrorHandlingPolicy(
        loadErrorHandlingPolicy: LoadErrorHandlingPolicy
    ): MediaSource.Factory {
        factory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        return this
    }

    override fun getSupportedTypes(): IntArray {
        return factory.supportedTypes
    }

    override fun createMediaSource(
        mediaItem: MediaItem,
    ): MediaSource = FakeMediaSource(
        factory = factory,
        mediaItem = mediaItem,
        coroutineScope = coroutineScope,
    )
}
