package androidx.media3.demo.session.source

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.Allocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

private const val TAG = "FakeMediaSource"

@OptIn(UnstableApi::class)
internal class FakeMediaSource(
    private val factory: MediaSource.Factory,
    private val mediaItem: MediaItem,
    private val coroutineScope: CoroutineScope,
) : BaseMediaSource() {

    val uris = listOf(
        "https://dl.dropboxusercontent.com/scl/fi/31mk3pcdyi0vjfphwmy5n/1c7c50853ef070899a8bb3a6a353dbc9e79b11b294bfe2f99277ed056ca9.ogg?rlkey=xf0ymfdfad7dnyye4bx4isw2o&st=vcnz3b1z&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/77zmpujl3130xmdjakfzk/1da1d56a2a0e65464d0e9b2a81d854299426461cc2071f8aece818fd33dc.ogg?rlkey=ttnzgmzmiiv0wzmu356lpjeeg&st=at3stfld&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/aufx2p5wwr0ir4bc0exso/1e297b816f975349e52fc520ff1477ba688f2f256909b9a37515f037ff14.ogg?rlkey=3g0m5oj456ofvyjpb4v07oesj&st=66mmdnwj&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/4vtfr7m6r08z1acfntqgr/4add0a8487c92a579a9081305d2c45ff40e576fc34e794c1acadf3f3e358.ogg?rlkey=ndnpv9bd0ywfhlfg5tforrpbp&st=02s828zm&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/2rco943tytqx6iftrrc2e/5fe0a4768a135bf621f49e95be1eeb35efa0953346536dc22894d189f632.ogg?rlkey=2c47iehphrzhphfzf4r65f697&st=jzc1d8v3&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/0whpa5aim085nd3vb6x2x/6e56a768ae69048fd3c78f9c1f4ed1315c0bb0e7f01aa3eb9e846fdab4d8.ogg?rlkey=ru4xsrlcx41gwil2t0nhbmq5z&st=wsuy5jqh&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/5njqvg38wj6rbnlwjyca1/7b4f5c1a39d1929ac3a05a9b7a7d9793cdc5e71e0c52d17ca6c09d59511b.ogg?rlkey=za06rg6bqfmdr8kjzfyvg526b&st=xz36ln66&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/oog0riso7ufbsxwh5sti5/7c4c0e37c81adc5af06f4c0b823de3df81686405499c87d7229aaa9bb684.ogg?rlkey=u3ucw2pfiu87zmoxfvsrjcd9l&st=xus4bazm&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/tazee87d4qiaikpn4ao70/7ccf403b7a102d31fa11bb35a2c64fe7c5cf714ea1a949d0da814675d40b.ogg?rlkey=9jyrawwbn4o2vzvxec53oydgc&st=wl3eopn9&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/9ikx73zrckcyqx4wenl8k/8b8d481a32217c42d4ceff943e0a18b3cc1833ded0ac25ea2844a5fc6844.ogg?rlkey=c8luj5u1nzrxjr979fhksqeja&st=myxw7btf&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/2cyav3kpc1y8ddlmblk88/9fd21d93eebc71c7cfb30d2923e56222f383a7df18f95ad14a6418127869.ogg?rlkey=7wanz738tvqnouct4sm4wx5vm&st=dhqwqodj&dl=0",
        "https://dl.dropboxusercontent.com/scl/fi/h5e28rvcviz5a53skrt6j/35b001fa5e64adf9762536ad7e7b76ce79952d6440507d49a5f23f30632d.ogg?rlkey=n9kk3dfi9pezwiosxnndcuxdk&st=lxgovd95&dl=0",
    )
    val durations = listOf(7000L, 2000L, 1000L, 1000L, 12000L, 2000L, 4000L, 3000L, 1000L, 3000L, 7000L, 11000L)

    private val mediaSourceCaller = MediaSource.MediaSourceCaller { source, timeline ->
        refreshSourceInfo(timeline)
    }

    private var concatenatingSource: ConcatenatingMediaSource2? = null

    override fun getMediaItem() = mediaItem

    override fun maybeThrowSourceInfoRefreshError() {
        // As in most other MediaSource implementations, do nothing.
    }

    override fun createPeriod(
        id: MediaSource.MediaPeriodId,
        allocator: Allocator,
        startPositionUs: Long
    ) = concatenatingSource?.createPeriod(id, allocator, startPositionUs)
        ?: error("ConcatenatingMediaSource2 not prepared yet")

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        concatenatingSource?.releasePeriod(mediaPeriod)
    }

    override fun releaseSourceInternal() {
        concatenatingSource?.releaseSource(mediaSourceCaller)
        concatenatingSource = null
    }

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        // prepareSourceInternalSynchronously(mediaTransferListener) // Note: this works
        prepareSourceInternalAsync(mediaTransferListener) // Fixme: This is causing the issue!
    }

    private fun prepareSourceInternalAsync(
        mediaTransferListener: TransferListener?,
    ) {
        coroutineScope.launch {
            try {
                val uris = uris
                val durations = durations
                delay(2.seconds) // Simulate background work

                withContext(context = Dispatchers.Main) {

                    prepareConcatenatingSource(
                        uris = uris,
                        durations = durations,
                        mediaTransferListener = mediaTransferListener,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to process prepareSourceInternal", e)
            }
        }
    }

    private fun prepareSourceInternalSynchronously(
        mediaTransferListener: TransferListener?,
    ) {
        try {
            prepareConcatenatingSource(
                uris = uris,
                durations = durations,
                mediaTransferListener = mediaTransferListener,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to process prepareSourceInternal", e)
        }
    }

    private fun prepareConcatenatingSource(
        uris: List<String>,
        durations: List<Long>,
        mediaTransferListener: TransferListener?,
    ) {
        concatenatingSource = ConcatenatingMediaSource2.Builder()
            .setMediaSourceFactory(factory)
            .setMediaItem(mediaItem)
            .apply {
                uris.forEachIndexed { index, uri ->
                    add(
                        factory.createMediaSource(MediaItem.fromUri(uri)),
                        durations[index],
                    )
                }
            }
            .build()
            .also {
                it.prepareSource(
                    mediaSourceCaller,
                    mediaTransferListener,
                    playerId,
                )
            }
    }
}
