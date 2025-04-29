import { useEffect, useState } from "react";

const ListOfConnectedDevices = () => {
    const [deviceList, setDeviceList] = useState<any[]>([]);
    useEffect(() => {
        window.ipcRenderer.on("connected_device_list", (e, data) => {
            console.log("Data", data);
            setDeviceList(data);
        });
    }, []);

    async function chooseFolder(device: any) {
        const filePath = await window.ipcRenderer.invoke(
            "choose_folder_for_device",
            device
        );
        if (filePath) {
            console.log("Selected folder:", filePath);
        } else {
            console.log("Folder selection was canceled.");
        }
    }
    return (
        <div className="px-2 py-2">
            <p className="text-xl font-bold">Device Lists</p>
            <div className="flex flex-col gap-2">
                {deviceList?.map((item) => {
                    return (
                        <div
                            key={item?.deviceId}
                            className="bg-black/90 rounded-md px-4 py-3 flex flex-col gap-2"
                        >
                            <p className="font-bold font-lg">
                                {item?.deviceName}
                            </p>
                            <p>{item?.deviceId}</p>
                            {item?.fileSavePath && (
                                <p>File Path: {item?.fileSavePath}</p>
                            )}

                            <button
                                onClick={() => chooseFolder(item)}
                                className="btn btn-primary btn-outline"
                            >
                                Choose File Path
                            </button>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default ListOfConnectedDevices;
