//Vmax
package org.lineageos.settings.device.dac.utils;

import android.content.Context;

import org.lineageos.settings.device.dac.R;

import java.text.NumberFormat;

import vendor.lge.hardware.audio.dac.control.V2_1.DacState;
import vendor.lge.hardware.audio.dac.control.V2_1.Impedance;
import vendor.lge.hardware.audio.dac.control.V2_1.OutputStatus;

/* Panel and tile text for the driver's output status */
public final class DacStatus {

    private DacStatus() {}

    public static String formatRate(Context context, int hz) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(1);
        return context.getString(R.string.dac_rate_khz, format.format(hz / 1000.0));
    }

    private static boolean isFloating(OutputStatus status) {
        return status.impedanceLeft == Impedance.FLOATING
                || status.impedanceRight == Impedance.FLOATING;
    }

    // IDLE drops to BYPASS a few seconds after the stream stops; keep re-reading until then
    public static boolean isSettling(OutputStatus status) {
        return status != null && status.state == DacState.IDLE;
    }

    public static String output(Context context, OutputStatus status, boolean enabled) {
        if (status == null)
            return context.getString(R.string.dac_status_unavailable);
        switch (status.state) {
            case DacState.HIFI:
                if (status.dop > 0)
                    return context.getString(R.string.dac_output_hifi_dop, status.dop);
                return context.getString(status.osfBypass ? R.string.dac_output_hifi_osf_bypass
                        : R.string.dac_output_hifi,
                        formatRate(context, status.sampleRate), status.bitDepth);
            case DacState.IDLE:
                return context.getString(R.string.dac_output_idle);
            case DacState.BYPASS:
                return context.getString(enabled ? R.string.dac_output_standby
                        : R.string.dac_output_bypass);
            default:
                return context.getString(R.string.dac_output_off);
        }
    }

    private static String impedance(Context context, OutputStatus status, boolean compact) {
        if (isFloating(status))
            return context.getString(R.string.dac_impedance_floating);
        if (status.impedanceLeft == status.impedanceRight)
            return context.getString(R.string.dac_impedance, status.impedanceLeft);
        return context.getString(compact ? R.string.dac_impedance_lr_short
                : R.string.dac_impedance_lr, status.impedanceLeft, status.impedanceRight);
    }

    public static String headphones(Context context, OutputStatus status, int selectedMode) {
        if (status == null)
            return context.getString(R.string.dac_status_unavailable);
        return impedance(context, status, false);
    }

    // Short form for the QS tile, null when there is nothing useful to add
    public static String tileSubtitle(Context context, OutputStatus status) {
        if (status == null)
            return null;
        String state;
        switch (status.state) {
            case DacState.HIFI:
                state = status.dop > 0 ? context.getString(R.string.dac_tile_dop, status.dop)
                        : context.getString(R.string.dac_tile_format,
                                formatRate(context, status.sampleRate), status.bitDepth);
                break;
            case DacState.IDLE:
            case DacState.BYPASS:
                state = context.getString(R.string.dac_tile_standby);
                break;
            default:
                return null;
        }
        return state;
    }
}
