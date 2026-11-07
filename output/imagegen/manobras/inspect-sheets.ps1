Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
public static class TrickSpriteBounds {
    public static int[][] Components(Bitmap bitmap, int columns, int rows) {
        int w = bitmap.Width, h = bitmap.Height;
        bool[] opaque = new bool[w*h], seen = new bool[w*h];
        int[] queue = new int[w*h];
        int[][] bounds = new int[columns*rows][];
        for (int i=0; i<bounds.Length; i++) bounds[i] = new int[] {w,h,-1,-1};
        for (int y=0; y<h; y++) for (int x=0; x<w; x++) opaque[y*w+x] = bitmap.GetPixel(x,y).A >= 160;
        for (int seed=0; seed<opaque.Length; seed++) {
            if (!opaque[seed] || seen[seed]) continue;
            int head=0, tail=1, minX=w, minY=h, maxX=-1, maxY=-1;
            queue[0]=seed; seen[seed]=true;
            while(head<tail) {
                int p=queue[head++], x=p%w, y=p/w;
                minX=Math.Min(minX,x); minY=Math.Min(minY,y); maxX=Math.Max(maxX,x); maxY=Math.Max(maxY,y);
                for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++) {
                    int nx=x+dx, ny=y+dy;
                    if(nx<0 || nx>=w || ny<0 || ny>=h) continue;
                    int next=ny*w+nx;
                    if(!seen[next] && opaque[next]) { seen[next]=true; queue[tail++]=next; }
                }
            }
            if(tail<10) continue;
            int col=Math.Min(columns-1, ((minX+maxX)*columns)/(2*w));
            int row=Math.Min(rows-1, ((minY+maxY)*rows)/(2*h));
            int[] b=bounds[row*columns+col];
            b[0]=Math.Min(b[0],minX); b[1]=Math.Min(b[1],minY); b[2]=Math.Max(b[2],maxX); b[3]=Math.Max(b[3],maxY);
        }
        for(int i=0;i<bounds.Length;i++) {
            int[] b=bounds[i];
            if(b[2]<0) { bounds[i]=new int[]{0,0,0,0}; continue; }
            b[2]=b[2]-b[0]+1; b[3]=b[3]-b[1]+1;
        }
        return bounds;
    }
    public static int[] Scan(Bitmap bitmap, int x0, int y0, int width, int height) {
        int minX = width, minY = height, maxX = -1, maxY = -1;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (bitmap.GetPixel(x0 + x, y0 + y).A >= 160) {
                    minX = Math.Min(minX, x); minY = Math.Min(minY, y);
                    maxX = Math.Max(maxX, x); maxY = Math.Max(maxY, y);
                }
        if (maxX < 0) return new int[] {0, 0, 0, 0};
        return new int[] {minX, minY, maxX-minX+1, maxY-minY+1};
    }
}
'@
$trickAssets = Get-Content -Raw -LiteralPath 'output/imagegen/manobras/generated_files.json' | ConvertFrom-Json
$sheetMetadata = foreach ($entry in $trickAssets) {
    $bitmap = [System.Drawing.Bitmap]::new((Join-Path (Get-Location) ('core/assets/manobras/' + $entry.id + '.png')))
    try {
        $components = [TrickSpriteBounds]::Components($bitmap, $entry.columns, $entry.rows)
        $frames = for ($row = 0; $row -lt $entry.rows; $row++) {
            for ($col = 0; $col -lt $entry.columns; $col++) {
                $x0 = [int][Math]::Floor($col * $bitmap.Width / $entry.columns)
                $y0 = [int][Math]::Floor($row * $bitmap.Height / $entry.rows)
                $x1 = [int][Math]::Floor(($col + 1) * $bitmap.Width / $entry.columns)
                $y1 = [int][Math]::Floor(($row + 1) * $bitmap.Height / $entry.rows)
                $bounds = $components[$row*$entry.columns+$col]
                $left = [Math]::Max(0, $bounds[0]-2)
                $top = [Math]::Max(0, $bounds[1]-2)
                $right = [Math]::Min($bitmap.Width, $bounds[0]+$bounds[2]+2)
                $bottom = [Math]::Min($bitmap.Height, $bounds[1]+$bounds[3]+2)
                [pscustomobject]@{index=$row*$entry.columns+$col; row=$row; column=$col; cellRect=@($x0,$y0,($x1-$x0),($y1-$y0)); rect=@($left,$top,($right-$left),($bottom-$top)); contentBounds=@($bounds[0],$bounds[1],$bounds[2],$bounds[3])}
            }
        }
        [pscustomobject]@{id=$entry.id; image=($entry.id+'.png'); width=$bitmap.Width; height=$bitmap.Height; columns=$entry.columns; rows=$entry.rows; frames=@($frames)}
    } finally { $bitmap.Dispose() }
}
$sheetMetadata | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 -LiteralPath 'output/imagegen/manobras/sheet-metadata-final.json' -ErrorAction Stop
$sheetMetadata | ForEach-Object { Write-Output ($_.id + ': ' + $_.frames.Count + ' quadros, ' + $_.width + 'x' + $_.height) }
