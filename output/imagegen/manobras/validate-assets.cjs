"use strict";
const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const root = path.resolve(__dirname, "../../..");
const assetRoot = path.join(root, "core/assets/manobras");
const manifest = JSON.parse(fs.readFileSync(path.join(assetRoot, "manifest.json"), "utf8"));
const enumSource = fs.readFileSync(path.join(root, "core/src/main/java/com/skate2k/logica/TipoManobra.java"), "utf8");
const expected = [...enumSource.matchAll(/^\s+([A-Z][A-Z_0-9]+)\("/gm)].map(m => m[1]).sort();
assert.deepEqual(manifest.actions.map(a => a.id).sort(), expected);
let totalFrames = 0;
for (const sheet of Object.values(manifest.sheets)) {
  const png = fs.readFileSync(path.join(assetRoot, sheet.image));
  assert.equal(png.subarray(0,8).toString("hex"), "89504e470d0a1a0a", sheet.id);
  assert.equal(png.readUInt32BE(16), sheet.width, sheet.id);
  assert.equal(png.readUInt32BE(20), sheet.height, sheet.id);
  assert.equal(png[25], 6, sheet.id + " must have RGBA channels");
  assert.equal(sheet.frames.length, sheet.rows * sheet.columns);
  totalFrames += sheet.frames.length;
  for (const f of sheet.frames) {
    const [x,y,w,h] = f.rect;
    assert(x>=0 && y>=0 && w>0 && h>0 && x+w<=sheet.width && y+h<=sheet.height, sheet.id);
    assert(f.contentBounds[2]>0 && f.contentBounds[3]>0, sheet.id + " empty frame");
  }
}
for (const [key,clip] of Object.entries(manifest.clips)) {
  for (const f of clip.frames) {
    assert(manifest.sheets[f.sheet]?.frames[f.index], key + " bad frame");
    assert(f.duration > 0, key + " duration");
  }
  if (clip.type.startsWith("GIRO_")) {
    const r = clip.rotationFrames;
    assert.equal(r.length, clip.degrees / 45 + 1);
    assert.equal(manifest.turntableViews[r[0].index], clip.direction);
    assert.equal(manifest.turntableViews[r.at(-1).index], clip.endDirection);
  }
  if (clip.type === "VARIAL") {
    assert.equal(manifest.turntableViews[clip.rotationFrames[0].index], clip.direction);
    assert.equal(manifest.turntableViews[clip.rotationFrames.at(-1).index], manifest.oppositeDirections[clip.direction]);
  }
}
for (const [alias,target] of Object.entries(manifest.aliases)) {
  assert(manifest.clips[target], alias);
  const direction = alias.split("/")[1];
  assert.equal(manifest.clips[target].direction, manifest.oppositeDirections[direction]);
}
for(const action of manifest.actions) for(const dir of manifest.directions) {
  assert(Object.values(manifest.clips).some(c=>c.type===action.id && c.direction===dir), action.id + "/" + dir);
}
for(const filename of ["previa.html","previa.js","manifest.js","README.md"]) assert(fs.existsSync(path.join(assetRoot,filename)));
console.log(JSON.stringify({pngs:Object.keys(manifest.sheets).length,frames:totalFrames,types:expected.length,clips:Object.keys(manifest.clips).length,fakieAliases:Object.keys(manifest.aliases).length,result:"OK"}));

