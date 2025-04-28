import express, { NextFunction, Request, Response } from "express";
import http from "http";
// import { Server } from "socket.io";
import { startBonjour } from "./bonjour";
import rootRoutes from "./routes";
export function errorHandler(
    err: any,
    req: Request,
    res: Response,
    next: NextFunction
) {
    console.error(err); // You can customize logging (save to file, etc.)

    const statusCode = err.statusCode || 500;
    const message = err.message || "Internal Server Error";

    res.status(statusCode).json({
        success: false,
        message,
        // stack: process.env.NODE_ENV === 'development' ? err.stack : undefined // optional: only in dev
    });
}

export async function startSyncServer() {
    const app = express();
    app.use(express.json());
    app.use("/api", rootRoutes);

    const server = http.createServer(app);
    // const io = new Server(server, {
    //     transports: ["websocket"],
    //     cors: {
    //         credentials: true,
    //     },
    // });

    // Start service discovery
    startBonjour(3001); // Different port than Vite

    // Handle connections
    // io.on("connection", (socket) => {
    //     console.log("Client connected:", socket.id);
    // });

    server.listen(3001, () => {
        console.log("Sync server running on port 3001");
    });

    app.use(errorHandler);

    // return io;
}
