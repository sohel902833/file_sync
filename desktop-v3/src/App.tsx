import { main_bg } from "@/assets";
import ListOfConnectedDevices from "./components/ListOnConnectedDevices";
function App() {
    console.log("Hello", window.ipcRenderer);
    return (
        <>
            <div
                style={{
                    background: `url(${main_bg})`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                }}
                className="h-screen grid grid-cols-12"
            >
                <div className="col-span-12">
                    <ListOfConnectedDevices />
                </div>
                {/* <div className="col-span-4">Hello</div>
                <div className="col-span-4"> */}
                {/* <div className="flex items-center justify-center h-screen flex-col">
                        <div className="relative w-24 h-24">
                            <div className="absolute inset-0 rounded-full border-4 border-blue-400 animate-ping"></div>

                            <div className="absolute inset-2 rounded-full border-4 border-blue-500 animate-ping delay-200"></div>

                            <div className="absolute inset-4 rounded-full border-4 border-blue-600 animate-pulse"></div>

                            <div>Hello</div>
                        </div>
                    </div> */}
                {/* </div> */}
            </div>
        </>
    );
}

export default App;
