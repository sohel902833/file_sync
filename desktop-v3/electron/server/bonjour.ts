import bonjour from "bonjour";
import os from "os";
export function startBonjour(port: number) {
    const instance = bonjour();
    const deviceName = os.hostname();
    instance.publish({
        name: deviceName,
        type: "filesync",
        port: port,
    });

    console.log(`Bonjour service advertising on port ${port}`);
    // Cleanup on exit
    process.on("exit", () => {
        instance.unpublishAll();
    });
}
