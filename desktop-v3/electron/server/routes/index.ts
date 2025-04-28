import express from "express";
import connectionRoute from "./app-connection-routes";
import uploadRoute from "./file-uploader-routes";

const router = express.Router();
interface IRoute {
    path: string;
    route: any;
}
const moduleRoutes: IRoute[] = [
    {
        path: "/connection",
        route: connectionRoute,
    },
    {
        path: "/upload",
        route: uploadRoute,
    },
];

moduleRoutes.forEach((route) => router.use(route.path, route.route));

export default router;
