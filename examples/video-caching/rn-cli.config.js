"use strict";

const path = require("path");
const blacklist = require("metro").createBlacklist;

const rootProjectDir = path.resolve(__dirname, "..", "..");

module.exports = {
  // Resolve react-native-video-exp from parent directory so we do not have to install react-native-video-exp after each change applied
  getBlacklistRE: function () {
    return blacklist([
      /node_modules\/react-native-video-exp\/.*/,
      new RegExp(`${rootProjectDir}/node_modules/react-native/.*`),
    ]);
  },
  getProjectRoots() {
    return [__dirname, rootProjectDir];
  },
};
