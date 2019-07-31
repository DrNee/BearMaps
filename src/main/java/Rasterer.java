import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private double rasterUlLon;
    private double rasterUlLat;
    private double rasterLrLon;
    private double rasterLrLat;

    public Rasterer() {
        // YOUR CODE HERE
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "rasterUlLon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "rasterUlLat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "rasterLrLon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "rasterLrLat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        //System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
        //        + "your browser.");
        int depth = imageToDepth(params.get("lrlon"), params.get("ullon"), params.get("w"));
        String[][] renderGrid = findBoundingBox(depth, params.get("ullon"),
            params.get("lrlon"), params.get("lrlat"), params.get("ullat"));
        results.put("depth", depth);
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", rasterUlLon);
        results.put("raster_ul_lat", rasterUlLat);
        results.put("raster_lr_lon", rasterLrLon);
        results.put("raster_lr_lat", rasterLrLat);
        results.put("query_success", true);
        return results;
    }

    private int imageToDepth(double lrlon, double ullon, double w) {
        double imageLonDPP = Math.abs(lrlon - ullon) / w;
        double rootLonDPP = Math.abs(MapServer.ROOT_LRLON
            - MapServer.ROOT_ULLON) / MapServer.TILE_SIZE;
        // depth = Math.floor(k) such that rootLonDPP/2^(k-1) = imageLonDPP; if k > 7, k = 7
        // 2^(k-1) = rootLonDPP/imageLonDPP => k = log(rootLonDPP/imageLonDPP)/log(2) + 1
        double depth = 1 + (Math.log(rootLonDPP / imageLonDPP) / Math.log(2));
        if (depth > 7) {
            return 7;
        }
        return (int) Math.floor(depth);
    }

    private String[][] findBoundingBox(int depth, double ullon,
        double lrlon, double lrlat, double ullat) {
        double lonPerBox = Math.abs(MapServer.ROOT_ULLON
            - MapServer.ROOT_LRLON) / Math.pow(2, depth);
        double latPerBox = Math.abs(MapServer.ROOT_ULLAT
            - MapServer.ROOT_LRLAT) / Math.pow(2, depth);
//      find the distance per image horizontally and vertically at the corresponding depth
        int x1 = (int) (Math.abs(MapServer.ROOT_ULLON
            - ullon) / lonPerBox);
        // bounding box upperleft x
        int x2 = (int) Math.pow(2, depth)
            - (int) (Math.abs(MapServer.ROOT_LRLON - lrlon) / lonPerBox);
        // bb lower right x
        int y1 = (int) ((Math.abs(MapServer.ROOT_ULLAT - ullat) / latPerBox));
        // bb upper left y
        int y2 = (int) Math.pow(2, depth)
            - (int) (Math.abs(MapServer.ROOT_LRLAT - lrlat) / latPerBox);
        // bb lower right y

        rasterUlLon = MapServer.ROOT_ULLON + (x1 * lonPerBox);
        rasterLrLon = MapServer.ROOT_ULLON + (x2 * lonPerBox);
        rasterUlLat = MapServer.ROOT_ULLAT - (y1 * latPerBox);
        rasterLrLat = MapServer.ROOT_ULLAT - (y2 * latPerBox);

        String[][] renderGrid = new
            String[y2 - y1][x2 - x1];
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                renderGrid[y - y1][x - x1] = "d" + String.valueOf(depth) + "_x"
                    + String.valueOf(x) + "_y" + String.valueOf(y) + ".png";
            }
        }
        return renderGrid;
    }
}
