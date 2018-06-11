package org.itzheng.and.ble.callback;

/**
 * Title:<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-1-22.
 */

public interface OnBleSwitchStatusChangeListener {
    /**
     * 蓝牙模块打开
     */
    public void onStateOn();

    /**
     * 蓝牙模块关闭
     */
    public void onStateOff();


}
