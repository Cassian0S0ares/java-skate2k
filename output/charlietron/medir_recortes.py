# Apenas mede a arte original; não altera os PNGs.
from PIL import Image
from collections import deque
from pathlib import Path
import json
for path in Path('core/assets/charlietron').glob('*.png'):
 im=Image.open(path);w,h=im.size;a=im.getchannel('A');pix=list(a.getdata());seen=bytearray(w*h);groups=[[] for _ in range(32)]
 for p in range(w*h):
  if seen[p] or pix[p]<=128:continue
  queue=deque([p]);seen[p]=1;pts=[]
  while queue:
   k=queue.popleft();pts.append(k);x=k%w;y=k//w
   for nx,ny in ((x-1,y),(x+1,y),(x,y-1),(x,y+1)):
    if 0<=nx<w and 0<=ny<h:
     n=ny*w+nx
     if not seen[n] and pix[n]>128:seen[n]=1;queue.append(n)
  if len(pts)<30:continue
  xs=[v%w for v in pts];ys=[v//w for v in pts];box=[min(xs),min(ys),max(xs)+1,max(ys)+1]
  col=min(7,int((box[0]+box[2])/2/(w/8)));row=min(3,int((box[1]+box[3])/2/(h/4)))
  groups[row*8+col].append((box,[p%w,p//w],len(pts)))
 frames=[]
 for i,g in enumerate(groups):
  assert g,(path,i)
  x=max(0,min(v[0][0] for v in g)-2);y=max(0,min(v[0][1] for v in g)-2)
  right=min(w,max(v[0][2] for v in g)+2);bottom=min(h,max(v[0][3] for v in g)+2)
  frames.append({'rect':[x,y,right-x,bottom-y],'seeds':[[v[1][0]-x,v[1][1]-y] for v in g],'opaquePixels':sum(v[2] for v in g)})
 path.with_suffix('.json').write_text(json.dumps({'image':path.name,'frames':frames},indent=2)+'\n')
 print(path.name,'components',sum(map(len,groups)),'max size',max(f['rect'][2] for f in frames),max(f['rect'][3] for f in frames))
