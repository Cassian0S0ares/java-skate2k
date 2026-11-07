"use strict";
(() => {
  const data = window.SKATE_MANOBRAS;
  const el = id => document.getElementById(id);
  const canvas = el("canvas"), ctx = canvas.getContext("2d");
  const images = new Map();
  let clip, elapsed = 0, paused = false, last = 0, frameIndex = 0;
  const cyclic = type => type === "VARIAL" || type.startsWith("GIRO_");
  for (const action of data.actions) {
    const option = document.createElement("option");
    option.value = action.id; option.textContent = action.title;
    el("action").append(option);
  }
  function selectClip() {
    const type = el("action").value;
    el("spin").disabled = !cyclic(type);
    const spin = cyclic(type) ? el("spin").value : "nenhum";
    let key = [type, el("direction").value, spin, el("stance").value].join("/");
    key = data.aliases[key] || key;
    clip = data.clips[key];
    if (!clip) throw new Error("Sequência ausente: " + key);
    elapsed = 0; frameIndex = 0;
    el("scrub").max = clip.frames.length - 1;
    let detail = clip.title + " · corpo: " + clip.direction + " → " + clip.endDirection + ".";
    if (clip.togglesFakie) detail += " Termina com a orientação invertida.";
    if (clip.holdLoop) detail += " Inclui poses para sustentar a manobra.";
    el("details").textContent = detail;
  }
  function cumulative(index) {
    return clip.frames.slice(0, index).reduce((total, f) => total + f.duration, 0);
  }
  function currentIndex(time) {
    let total = 0;
    for (let i = 0; i < clip.frames.length; i++) {
      total += clip.frames[i].duration;
      if (time < total) return i;
    }
    return clip.frames.length - 1;
  }
  function draw() {
    if (!clip) return;
    const frameRef = clip.frames[frameIndex];
    const sheet = data.sheets[frameRef.sheet], frame = sheet.frames[frameRef.index];
    const img = images.get(frameRef.sheet);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (img) {
      const [sx, sy, sw, sh] = frame.rect;
      const scale = sheet.previewScale * 1.25;
      ctx.imageSmoothingEnabled = true; ctx.imageSmoothingQuality = "high";
      ctx.drawImage(img, sx, sy, sw, sh, (canvas.width - sw * scale) / 2, 350 - sh * scale, sw * scale, sh * scale);
    }
    el("frame-count").textContent = (frameIndex + 1) + " / " + clip.frames.length;
    el("scrub").value = frameIndex;
    el("atlas-link").href = sheet.image;
  }
  function tick(now) {
    const dt = last ? Math.min((now - last) / 1000, 0.1) : 0;
    last = now;
    if (clip && !paused) {
      const duration = clip.frames.reduce((sum, f) => sum + f.duration, 0);
      elapsed = (elapsed + dt * Number(el("speed").value)) % duration;
      frameIndex = currentIndex(elapsed);
    }
    draw(); requestAnimationFrame(tick);
  }
  for (const id of ["action", "direction", "spin", "stance"]) el(id).addEventListener("change", selectClip);
  el("play").addEventListener("click", () => {
    paused = !paused; el("play").textContent = paused ? "Reproduzir" : "Pausar";
  });
  el("next").addEventListener("click", () => {
    paused = true; el("play").textContent = "Reproduzir";
    frameIndex = (frameIndex + 1) % clip.frames.length; elapsed = cumulative(frameIndex);
    draw();
  });
  el("scrub").addEventListener("input", () => {
    paused = true; el("play").textContent = "Reproduzir";
    frameIndex = Number(el("scrub").value); elapsed = cumulative(frameIndex); draw();
  });
  el("speed").addEventListener("input", () => {
    el("speed-label").textContent = Number(el("speed").value).toLocaleString("pt-BR") + "×";
  });
  Promise.all(Object.values(data.sheets).map(sheet => new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => { images.set(sheet.id, img); resolve(); };
    img.onerror = () => reject(new Error("Não foi possível abrir " + sheet.image + ". Mantenha a prévia na pasta dos PNGs."));
    img.src = sheet.image;
  }))).then(() => { selectClip(); requestAnimationFrame(tick); }).catch(error => {
    el("error").textContent = error.message;
  });
})();
