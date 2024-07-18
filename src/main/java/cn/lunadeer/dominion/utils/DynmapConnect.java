package cn.lunadeer.dominion.utils;

import cn.lunadeer.dominion.dtos.DominionDTO;
import cn.lunadeer.minecraftpluginutils.Scheduler;
import cn.lunadeer.minecraftpluginutils.XLogger;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.List;
import java.util.Map;

public class DynmapConnect extends DynmapCommonAPIListener {

    public static DynmapConnect instance;

    private MarkerSet markerSet_dominion;
    private MarkerSet markerSet_mca;

    public DynmapConnect() {
        instance = this;
    }

    @Override
    public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
        MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        this.markerSet_dominion = markerAPI.getMarkerSet("dominion");
        if (this.markerSet_dominion == null) {
            this.markerSet_dominion = markerAPI.createMarkerSet("dominion", "Dominion领地", null, false);
        }
        if (this.markerSet_mca == null) {
            this.markerSet_mca = markerAPI.createMarkerSet("mca", "MCA文件", null, false);
        }
    }

    public void setDominionMarker(DominionDTO dominion) {
        String nameLabel = "<div>" + dominion.getName() + "</div>";
        double[] xx = {dominion.getX1(), dominion.getX2()};
        double[] zz = {dominion.getZ1(), dominion.getZ2()};
        AreaMarker marker = this.markerSet_dominion.createAreaMarker(
                dominion.getId().toString(),
                nameLabel,
                true,
                dominion.getWorld(),
                xx,
                zz,
                false
        );
        marker.setFillStyle(0.2, dominion.getColorHex());
        marker.setLineStyle(1, 0.8, dominion.getColorHex());
        XLogger.debug("Add dominion marker: " + dominion.getName());
    }

    public void setDominionMarkers(List<DominionDTO> dominions) {
        Scheduler.runTaskAsync(() -> {
            this.markerSet_dominion.getAreaMarkers().forEach(AreaMarker::deleteMarker);
            for (DominionDTO dominion : dominions) {
                this.setDominionMarker(dominion);
            }
        });
    }

    public void setMCAMarkers(Map<String, List<String>> mca_files) {
        Scheduler.runTaskAsync(() -> {
            this.markerSet_mca.getAreaMarkers().forEach(AreaMarker::deleteMarker);
            for (Map.Entry<String, List<String>> entry : mca_files.entrySet()) {
                for (String file : entry.getValue()) {
                    String[] cords = file.split("\\.");
                    int world_x1 = Integer.parseInt(cords[1]) * 512;
                    int world_x2 = (Integer.parseInt(cords[1]) + 1) * 512;
                    int world_z1 = Integer.parseInt(cords[2]) * 512;
                    int world_z2 = (Integer.parseInt(cords[2]) + 1) * 512;
                    String nameLabel = "<div>" + file + "</div>";
                    double[] xx = {world_x1, world_x2};
                    double[] zz = {world_z1, world_z2};
                    AreaMarker marker = this.markerSet_mca.createAreaMarker(
                            file,
                            nameLabel,
                            true,
                            entry.getKey(),
                            xx,
                            zz,
                            false
                    );
                    marker.setFillStyle(0.2, 0x00CC00);
                    marker.setLineStyle(1, 0.8, 0x00CC00);
                }
            }
        });
    }

}
