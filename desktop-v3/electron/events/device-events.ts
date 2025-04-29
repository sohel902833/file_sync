import { readDevices } from "../db";
import { win } from "../main";

export const sendConnectedDevice = () => {
    if (win) {
        const deviceList = readDevices();
        win?.webContents?.send("connected_device_list", deviceList);
    }
};
