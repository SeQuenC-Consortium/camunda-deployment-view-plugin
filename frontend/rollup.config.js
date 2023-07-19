import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import replace from "@rollup/plugin-replace";
import json from '@rollup/plugin-json';
import scss from "rollup-plugin-scss";

export default {
    input: "src/plugin.js",
    output: {
        file: "dist/plugin.js"
    },
    plugins: [
        json(),
        resolve(),
        commonjs({
            include: "node_modules/**"
        }),
        replace({
            "process.env.NODE_ENV": JSON.stringify("production")
        }),
        scss({
            failOnError: true
        })
    ]
};