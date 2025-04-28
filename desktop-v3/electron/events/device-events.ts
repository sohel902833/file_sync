import { readDevices } from "../db";
import { win } from "../main";

export const sendConnectedDevice = () => {
    if (win) {
        const deviceList = readDevices();
        console.log("Read Device", deviceList);
        win?.webContents?.send("connected_device_list", deviceList);
    }
};
