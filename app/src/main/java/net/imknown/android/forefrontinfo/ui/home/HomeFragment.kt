package net.imknown.android.forefrontinfo.ui.home

import android.os.Bundle
import com.topjohnwu.superuser.Shell
import net.imknown.android.forefrontinfo.BuildConfig
import net.imknown.android.forefrontinfo.R
import net.imknown.android.forefrontinfo.base.BaseListFragment

class HomeFragment : BaseListFragment() {

    companion object {
        fun newInstance() = HomeFragment()

        init {
            // Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
            Shell.Config.setFlags(Shell.FLAG_NON_ROOT_SHELL)
            Shell.Config.verboseLogging(BuildConfig.DEBUG)
            // Shell.Config.setTimeout(10)
        }

        // /* root needed*/ private const val CMD_BOOT_PARTITION = "ls /dev/block/bootdevice/by-name | grep boot_"
        // private const val CMD_ROM_TOTAL_SIZE = "df | grep -v '/apex' | grep -v '/storage' | grep -E 'tmpfs'\\|'/dev'\\|'/data' | awk '{s+=\$2} END {print s/1000000}'"
        private const val CMD_AB_UPDATE = "getprop ro.build.ab_update"
        private const val CMD_SLOT_SUFFIX = "getprop ro.boot.slot_suffix"

        private const val CMD_TREBLE_ENABLED = "getprop ro.treble.enabled"

        private const val CMD_VNDK_LITE = "getprop ro.vndk.lite"
        private const val CMD_VNDK_VERSION = "getprop ro.vndk.version"

        private const val CMD_SAR = "mount | grep -E 'rootfs on / type rootfs'\\|'rootfs / rootfs'"

        private const val CMD_APEX_MOUNT = "mount | grep 'tmpfs on /apex type tmpfs'"
        private const val CMD_APEX_TZDATA = "mount | grep /apex/com.android.tzdata"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        showResult(collectionDataset())
    }

    private fun collectionDataset(): ArrayList<MyModel> {
        val myDataset = ArrayList<MyModel>()

        // region [A/B]
        // val bootPartitions = sh(CMD_BOOT_PARTITION)[0]

        // val romTotalSizeResult = sh(CMD_ROM_TOTAL_SIZE)
        // val romTotalSize = kotlin.math.floor(romTotalSizeResult[0].toFloat()).toString()

        val abUpdateSupportedResult = sh(CMD_AB_UPDATE)
        val isAbUpdateSupported =
            abUpdateSupportedResult.isNotEmpty() && abUpdateSupportedResult[0]!!.toBoolean()
        val abUpdateSupportedArgs =
            translate(isAbUpdateSupported) /* + getString(R.string.rom_total_size_result, romTotalSize) */
        myDataset.add(
            MyModel(
                getString(
                    R.string.ab_seamless_update_enabled_result,
                    abUpdateSupportedArgs
                ),
                getResultColor(isAbUpdateSupported)
            )
        )

        if (isAbUpdateSupported) {
            val slotSuffixUsing = sh(CMD_SLOT_SUFFIX)[0]
            myDataset.add(
                MyModel(
                    getString(R.string.current_using_ab_slot_result, slotSuffixUsing),
                    R.color.colorSupport
                )
            )
        }
        // endregion [A/B]

        // region [Treble]
        val trebleEnabledResult = sh(CMD_TREBLE_ENABLED)
        val isTrebleEnabled =
            trebleEnabledResult.isNotEmpty() && trebleEnabledResult[0]!!.toBoolean()
        myDataset.add(
            MyModel(
                getString(R.string.treble_enabled_result, translate(isTrebleEnabled)),
                getResultColor(isTrebleEnabled)
            )
        )
        // endregion [Treble]

        // region [VNDK]
        val hasVndkLiteResult = sh(CMD_VNDK_LITE)
        val hasVndkLite = hasVndkLiteResult.isNotEmpty() && hasVndkLiteResult[0]!!.toBoolean()

        val vndkVersionResult = sh(CMD_VNDK_VERSION)
        val hasVndkVersion = vndkVersionResult.isNotEmpty() && vndkVersionResult[0].isNotEmpty()

        val isVndkBuiltIn = hasVndkLite || hasVndkVersion

        var isVndkBuiltInResult = translate(isVndkBuiltIn)
        if (isVndkBuiltIn) {
            val vndkVersion = if (hasVndkVersion) {
                vndkVersionResult[0]
            } else {
                getString(android.R.string.unknownName)
            }

            isVndkBuiltInResult += getString(R.string.built_in_vndk_version_result, vndkVersion)
        }
        myDataset.add(
            MyModel(
                getString(R.string.vndk_built_in_result, isVndkBuiltInResult),
                getResultColor(isVndkBuiltIn)
            )
        )
        // endregion [VNDK]

        // region [SAR]
        val sarResult = sh(CMD_SAR)
        val isSar = sarResult.isEmpty() || sarResult[0].isEmpty()
        myDataset.add(
            MyModel(
                getString(R.string.sar_enabled_result, translate(isSar)),
                getResultColor(isSar)
            )
        )
        // endregion [SAR]

        // region [APEX]
        val apexMountedResult = sh(CMD_APEX_MOUNT)
        val isApexMounted = apexMountedResult.isNotEmpty() && apexMountedResult[0].isNotEmpty()

        val apexUsedResult = sh(CMD_APEX_TZDATA)
        val isApexUsed = apexUsedResult.isNotEmpty() && apexUsedResult[0].isNotEmpty()
        val isApex = isApexMounted && isApexUsed
        myDataset.add(
            MyModel(
                getString(R.string.apex_enabled_result, translate(isApex)),
                getResultColor(isApex)
            )
        )
        // endregion [APEX]

        return myDataset
    }

    private fun sh(cmd: String) = Shell.sh(cmd).exec().out

    private fun translate(condition: Boolean) = getString(
        if (condition) {
            R.string.result_yes
        } else {
            R.string.result_no
        }
    )

    private fun getResultColor(condition: Boolean) =
        if (condition) {
            R.color.colorSupport
        } else {
            R.color.colorNotSupport
        }
}