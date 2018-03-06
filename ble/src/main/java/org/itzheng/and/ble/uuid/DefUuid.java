package org.itzheng.and.ble.uuid;

/**
 * Title:<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-1-22.
 */
public class DefUuid implements IUUIDS {
    @Override
    public String getServiceUuid() {
        return "0000ffe0-0000-1000-8000-00805f9b34fb";
    }

    @Override
    public String getCharacteristicUuid() {
        return "0000ffe1-0000-1000-8000-00805f9b34fb";
//     //   另一个接口，目前没用
//        return "0000ffe2-0000-1000-8000-00805f9b34fb";
    }

    @Override
    public String getDescriptorUUid() {
        return "00002902-0000-1000-8000-00805f9b34fb";
    }
}
