/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
$(document).ready(function() {

    $(".click-title").mouseenter( function(    e){
        e.preventDefault();
        this.style.cursor="pointer";
    });
    $(".click-title").mousedown( function(event){
        event.preventDefault();
    });

    // Ugly code while this script is shared among several pages
    try{
        refreshHitsPerSecond(true);
    } catch(e){}
    try{
        refreshResponseTimeOverTime(true);
    } catch(e){}
    try{
        refreshResponseTimePercentiles();
    } catch(e){}
});


var responseTimePercentilesInfos = {
        data: {"result": {"minY": 64.0, "minX": 0.0, "maxY": 1643.0, "series": [{"data": [[0.0, 64.0], [0.1, 66.0], [0.2, 69.0], [0.3, 69.0], [0.4, 72.0], [0.5, 72.0], [0.6, 74.0], [0.7, 74.0], [0.8, 75.0], [0.9, 76.0], [1.0, 76.0], [1.1, 77.0], [1.2, 77.0], [1.3, 78.0], [1.4, 78.0], [1.5, 78.0], [1.6, 78.0], [1.7, 79.0], [1.8, 79.0], [1.9, 79.0], [2.0, 80.0], [2.1, 80.0], [2.2, 82.0], [2.3, 87.0], [2.4, 90.0], [2.5, 93.0], [2.6, 95.0], [2.7, 97.0], [2.8, 97.0], [2.9, 100.0], [3.0, 100.0], [3.1, 102.0], [3.2, 103.0], [3.3, 104.0], [3.4, 105.0], [3.5, 106.0], [3.6, 109.0], [3.7, 110.0], [3.8, 114.0], [3.9, 116.0], [4.0, 120.0], [4.1, 121.0], [4.2, 122.0], [4.3, 123.0], [4.4, 124.0], [4.5, 128.0], [4.6, 129.0], [4.7, 130.0], [4.8, 131.0], [4.9, 133.0], [5.0, 136.0], [5.1, 138.0], [5.2, 140.0], [5.3, 141.0], [5.4, 143.0], [5.5, 144.0], [5.6, 145.0], [5.7, 145.0], [5.8, 147.0], [5.9, 148.0], [6.0, 148.0], [6.1, 149.0], [6.2, 150.0], [6.3, 150.0], [6.4, 151.0], [6.5, 152.0], [6.6, 153.0], [6.7, 153.0], [6.8, 153.0], [6.9, 154.0], [7.0, 154.0], [7.1, 155.0], [7.2, 155.0], [7.3, 155.0], [7.4, 155.0], [7.5, 156.0], [7.6, 156.0], [7.7, 158.0], [7.8, 159.0], [7.9, 160.0], [8.0, 161.0], [8.1, 162.0], [8.2, 164.0], [8.3, 167.0], [8.4, 167.0], [8.5, 168.0], [8.6, 169.0], [8.7, 172.0], [8.8, 173.0], [8.9, 174.0], [9.0, 174.0], [9.1, 175.0], [9.2, 176.0], [9.3, 177.0], [9.4, 179.0], [9.5, 179.0], [9.6, 179.0], [9.7, 180.0], [9.8, 181.0], [9.9, 182.0], [10.0, 183.0], [10.1, 184.0], [10.2, 184.0], [10.3, 186.0], [10.4, 186.0], [10.5, 187.0], [10.6, 188.0], [10.7, 190.0], [10.8, 190.0], [10.9, 192.0], [11.0, 194.0], [11.1, 194.0], [11.2, 194.0], [11.3, 196.0], [11.4, 197.0], [11.5, 198.0], [11.6, 199.0], [11.7, 203.0], [11.8, 204.0], [11.9, 205.0], [12.0, 206.0], [12.1, 208.0], [12.2, 208.0], [12.3, 210.0], [12.4, 212.0], [12.5, 212.0], [12.6, 216.0], [12.7, 218.0], [12.8, 218.0], [12.9, 221.0], [13.0, 222.0], [13.1, 225.0], [13.2, 225.0], [13.3, 226.0], [13.4, 226.0], [13.5, 227.0], [13.6, 228.0], [13.7, 229.0], [13.8, 230.0], [13.9, 231.0], [14.0, 232.0], [14.1, 232.0], [14.2, 233.0], [14.3, 234.0], [14.4, 234.0], [14.5, 234.0], [14.6, 236.0], [14.7, 236.0], [14.8, 238.0], [14.9, 239.0], [15.0, 240.0], [15.1, 241.0], [15.2, 243.0], [15.3, 244.0], [15.4, 244.0], [15.5, 245.0], [15.6, 245.0], [15.7, 246.0], [15.8, 247.0], [15.9, 247.0], [16.0, 248.0], [16.1, 251.0], [16.2, 252.0], [16.3, 252.0], [16.4, 252.0], [16.5, 253.0], [16.6, 255.0], [16.7, 256.0], [16.8, 258.0], [16.9, 258.0], [17.0, 259.0], [17.1, 259.0], [17.2, 260.0], [17.3, 263.0], [17.4, 264.0], [17.5, 265.0], [17.6, 265.0], [17.7, 267.0], [17.8, 268.0], [17.9, 269.0], [18.0, 269.0], [18.1, 270.0], [18.2, 271.0], [18.3, 272.0], [18.4, 273.0], [18.5, 273.0], [18.6, 275.0], [18.7, 276.0], [18.8, 279.0], [18.9, 280.0], [19.0, 280.0], [19.1, 281.0], [19.2, 281.0], [19.3, 282.0], [19.4, 284.0], [19.5, 284.0], [19.6, 284.0], [19.7, 286.0], [19.8, 287.0], [19.9, 288.0], [20.0, 289.0], [20.1, 290.0], [20.2, 291.0], [20.3, 292.0], [20.4, 294.0], [20.5, 297.0], [20.6, 298.0], [20.7, 299.0], [20.8, 299.0], [20.9, 299.0], [21.0, 299.0], [21.1, 300.0], [21.2, 300.0], [21.3, 301.0], [21.4, 302.0], [21.5, 302.0], [21.6, 303.0], [21.7, 303.0], [21.8, 304.0], [21.9, 305.0], [22.0, 305.0], [22.1, 305.0], [22.2, 306.0], [22.3, 307.0], [22.4, 308.0], [22.5, 308.0], [22.6, 309.0], [22.7, 310.0], [22.8, 310.0], [22.9, 312.0], [23.0, 313.0], [23.1, 315.0], [23.2, 316.0], [23.3, 318.0], [23.4, 320.0], [23.5, 321.0], [23.6, 322.0], [23.7, 324.0], [23.8, 324.0], [23.9, 325.0], [24.0, 325.0], [24.1, 326.0], [24.2, 327.0], [24.3, 330.0], [24.4, 331.0], [24.5, 331.0], [24.6, 332.0], [24.7, 332.0], [24.8, 334.0], [24.9, 335.0], [25.0, 335.0], [25.1, 336.0], [25.2, 337.0], [25.3, 339.0], [25.4, 339.0], [25.5, 341.0], [25.6, 341.0], [25.7, 342.0], [25.8, 343.0], [25.9, 343.0], [26.0, 344.0], [26.1, 344.0], [26.2, 346.0], [26.3, 349.0], [26.4, 352.0], [26.5, 352.0], [26.6, 353.0], [26.7, 354.0], [26.8, 356.0], [26.9, 356.0], [27.0, 358.0], [27.1, 359.0], [27.2, 360.0], [27.3, 360.0], [27.4, 361.0], [27.5, 363.0], [27.6, 364.0], [27.7, 364.0], [27.8, 365.0], [27.9, 366.0], [28.0, 367.0], [28.1, 368.0], [28.2, 369.0], [28.3, 369.0], [28.4, 370.0], [28.5, 372.0], [28.6, 372.0], [28.7, 374.0], [28.8, 375.0], [28.9, 375.0], [29.0, 376.0], [29.1, 377.0], [29.2, 378.0], [29.3, 379.0], [29.4, 381.0], [29.5, 382.0], [29.6, 382.0], [29.7, 383.0], [29.8, 383.0], [29.9, 384.0], [30.0, 384.0], [30.1, 385.0], [30.2, 386.0], [30.3, 388.0], [30.4, 389.0], [30.5, 390.0], [30.6, 390.0], [30.7, 391.0], [30.8, 391.0], [30.9, 393.0], [31.0, 393.0], [31.1, 393.0], [31.2, 394.0], [31.3, 395.0], [31.4, 396.0], [31.5, 398.0], [31.6, 399.0], [31.7, 399.0], [31.8, 399.0], [31.9, 400.0], [32.0, 400.0], [32.1, 400.0], [32.2, 401.0], [32.3, 402.0], [32.4, 404.0], [32.5, 406.0], [32.6, 407.0], [32.7, 408.0], [32.8, 409.0], [32.9, 410.0], [33.0, 411.0], [33.1, 412.0], [33.2, 412.0], [33.3, 414.0], [33.4, 414.0], [33.5, 415.0], [33.6, 416.0], [33.7, 417.0], [33.8, 418.0], [33.9, 420.0], [34.0, 421.0], [34.1, 421.0], [34.2, 422.0], [34.3, 423.0], [34.4, 423.0], [34.5, 424.0], [34.6, 426.0], [34.7, 426.0], [34.8, 427.0], [34.9, 428.0], [35.0, 428.0], [35.1, 430.0], [35.2, 432.0], [35.3, 432.0], [35.4, 434.0], [35.5, 435.0], [35.6, 435.0], [35.7, 437.0], [35.8, 441.0], [35.9, 441.0], [36.0, 442.0], [36.1, 444.0], [36.2, 445.0], [36.3, 445.0], [36.4, 446.0], [36.5, 446.0], [36.6, 448.0], [36.7, 448.0], [36.8, 449.0], [36.9, 451.0], [37.0, 452.0], [37.1, 453.0], [37.2, 453.0], [37.3, 454.0], [37.4, 454.0], [37.5, 455.0], [37.6, 456.0], [37.7, 456.0], [37.8, 457.0], [37.9, 457.0], [38.0, 459.0], [38.1, 459.0], [38.2, 459.0], [38.3, 460.0], [38.4, 462.0], [38.5, 463.0], [38.6, 463.0], [38.7, 464.0], [38.8, 464.0], [38.9, 465.0], [39.0, 466.0], [39.1, 467.0], [39.2, 468.0], [39.3, 469.0], [39.4, 470.0], [39.5, 471.0], [39.6, 472.0], [39.7, 474.0], [39.8, 474.0], [39.9, 477.0], [40.0, 478.0], [40.1, 479.0], [40.2, 479.0], [40.3, 480.0], [40.4, 481.0], [40.5, 481.0], [40.6, 481.0], [40.7, 482.0], [40.8, 483.0], [40.9, 485.0], [41.0, 485.0], [41.1, 485.0], [41.2, 485.0], [41.3, 487.0], [41.4, 487.0], [41.5, 488.0], [41.6, 489.0], [41.7, 490.0], [41.8, 490.0], [41.9, 491.0], [42.0, 491.0], [42.1, 493.0], [42.2, 493.0], [42.3, 493.0], [42.4, 495.0], [42.5, 497.0], [42.6, 497.0], [42.7, 498.0], [42.8, 498.0], [42.9, 498.0], [43.0, 499.0], [43.1, 500.0], [43.2, 501.0], [43.3, 501.0], [43.4, 501.0], [43.5, 502.0], [43.6, 503.0], [43.7, 504.0], [43.8, 505.0], [43.9, 506.0], [44.0, 507.0], [44.1, 509.0], [44.2, 511.0], [44.3, 511.0], [44.4, 512.0], [44.5, 512.0], [44.6, 513.0], [44.7, 515.0], [44.8, 517.0], [44.9, 519.0], [45.0, 520.0], [45.1, 521.0], [45.2, 521.0], [45.3, 522.0], [45.4, 523.0], [45.5, 523.0], [45.6, 524.0], [45.7, 525.0], [45.8, 526.0], [45.9, 526.0], [46.0, 527.0], [46.1, 527.0], [46.2, 527.0], [46.3, 528.0], [46.4, 528.0], [46.5, 528.0], [46.6, 529.0], [46.7, 529.0], [46.8, 529.0], [46.9, 530.0], [47.0, 530.0], [47.1, 530.0], [47.2, 531.0], [47.3, 532.0], [47.4, 532.0], [47.5, 533.0], [47.6, 533.0], [47.7, 534.0], [47.8, 534.0], [47.9, 535.0], [48.0, 535.0], [48.1, 535.0], [48.2, 536.0], [48.3, 538.0], [48.4, 540.0], [48.5, 541.0], [48.6, 543.0], [48.7, 543.0], [48.8, 544.0], [48.9, 544.0], [49.0, 545.0], [49.1, 546.0], [49.2, 546.0], [49.3, 547.0], [49.4, 547.0], [49.5, 549.0], [49.6, 550.0], [49.7, 551.0], [49.8, 552.0], [49.9, 552.0], [50.0, 552.0], [50.1, 553.0], [50.2, 553.0], [50.3, 556.0], [50.4, 556.0], [50.5, 558.0], [50.6, 558.0], [50.7, 559.0], [50.8, 559.0], [50.9, 559.0], [51.0, 560.0], [51.1, 560.0], [51.2, 561.0], [51.3, 562.0], [51.4, 563.0], [51.5, 564.0], [51.6, 565.0], [51.7, 566.0], [51.8, 567.0], [51.9, 568.0], [52.0, 569.0], [52.1, 569.0], [52.2, 571.0], [52.3, 572.0], [52.4, 573.0], [52.5, 573.0], [52.6, 574.0], [52.7, 576.0], [52.8, 577.0], [52.9, 578.0], [53.0, 579.0], [53.1, 580.0], [53.2, 581.0], [53.3, 582.0], [53.4, 583.0], [53.5, 584.0], [53.6, 585.0], [53.7, 586.0], [53.8, 587.0], [53.9, 588.0], [54.0, 589.0], [54.1, 589.0], [54.2, 591.0], [54.3, 591.0], [54.4, 593.0], [54.5, 593.0], [54.6, 595.0], [54.7, 595.0], [54.8, 596.0], [54.9, 598.0], [55.0, 598.0], [55.1, 598.0], [55.2, 599.0], [55.3, 599.0], [55.4, 600.0], [55.5, 600.0], [55.6, 601.0], [55.7, 601.0], [55.8, 602.0], [55.9, 604.0], [56.0, 605.0], [56.1, 605.0], [56.2, 606.0], [56.3, 606.0], [56.4, 607.0], [56.5, 607.0], [56.6, 608.0], [56.7, 608.0], [56.8, 608.0], [56.9, 609.0], [57.0, 609.0], [57.1, 610.0], [57.2, 610.0], [57.3, 611.0], [57.4, 612.0], [57.5, 612.0], [57.6, 612.0], [57.7, 613.0], [57.8, 613.0], [57.9, 614.0], [58.0, 615.0], [58.1, 615.0], [58.2, 616.0], [58.3, 617.0], [58.4, 617.0], [58.5, 617.0], [58.6, 619.0], [58.7, 619.0], [58.8, 620.0], [58.9, 620.0], [59.0, 621.0], [59.1, 622.0], [59.2, 623.0], [59.3, 623.0], [59.4, 624.0], [59.5, 624.0], [59.6, 625.0], [59.7, 625.0], [59.8, 626.0], [59.9, 626.0], [60.0, 627.0], [60.1, 628.0], [60.2, 628.0], [60.3, 629.0], [60.4, 630.0], [60.5, 630.0], [60.6, 632.0], [60.7, 632.0], [60.8, 633.0], [60.9, 634.0], [61.0, 635.0], [61.1, 637.0], [61.2, 638.0], [61.3, 639.0], [61.4, 640.0], [61.5, 641.0], [61.6, 642.0], [61.7, 644.0], [61.8, 644.0], [61.9, 645.0], [62.0, 646.0], [62.1, 649.0], [62.2, 650.0], [62.3, 652.0], [62.4, 652.0], [62.5, 653.0], [62.6, 653.0], [62.7, 654.0], [62.8, 656.0], [62.9, 656.0], [63.0, 657.0], [63.1, 658.0], [63.2, 659.0], [63.3, 661.0], [63.4, 662.0], [63.5, 662.0], [63.6, 662.0], [63.7, 662.0], [63.8, 664.0], [63.9, 665.0], [64.0, 667.0], [64.1, 667.0], [64.2, 667.0], [64.3, 668.0], [64.4, 669.0], [64.5, 669.0], [64.6, 669.0], [64.7, 670.0], [64.8, 670.0], [64.9, 671.0], [65.0, 671.0], [65.1, 671.0], [65.2, 672.0], [65.3, 672.0], [65.4, 673.0], [65.5, 673.0], [65.6, 673.0], [65.7, 674.0], [65.8, 674.0], [65.9, 675.0], [66.0, 675.0], [66.1, 675.0], [66.2, 675.0], [66.3, 676.0], [66.4, 678.0], [66.5, 678.0], [66.6, 679.0], [66.7, 680.0], [66.8, 681.0], [66.9, 681.0], [67.0, 682.0], [67.1, 682.0], [67.2, 682.0], [67.3, 683.0], [67.4, 683.0], [67.5, 684.0], [67.6, 684.0], [67.7, 685.0], [67.8, 685.0], [67.9, 686.0], [68.0, 686.0], [68.1, 686.0], [68.2, 686.0], [68.3, 686.0], [68.4, 687.0], [68.5, 687.0], [68.6, 687.0], [68.7, 688.0], [68.8, 688.0], [68.9, 689.0], [69.0, 689.0], [69.1, 689.0], [69.2, 690.0], [69.3, 691.0], [69.4, 692.0], [69.5, 693.0], [69.6, 693.0], [69.7, 694.0], [69.8, 695.0], [69.9, 696.0], [70.0, 696.0], [70.1, 696.0], [70.2, 698.0], [70.3, 698.0], [70.4, 699.0], [70.5, 699.0], [70.6, 700.0], [70.7, 701.0], [70.8, 702.0], [70.9, 703.0], [71.0, 704.0], [71.1, 705.0], [71.2, 705.0], [71.3, 706.0], [71.4, 707.0], [71.5, 709.0], [71.6, 710.0], [71.7, 711.0], [71.8, 711.0], [71.9, 713.0], [72.0, 713.0], [72.1, 714.0], [72.2, 714.0], [72.3, 715.0], [72.4, 716.0], [72.5, 717.0], [72.6, 717.0], [72.7, 718.0], [72.8, 720.0], [72.9, 721.0], [73.0, 725.0], [73.1, 726.0], [73.2, 727.0], [73.3, 728.0], [73.4, 728.0], [73.5, 729.0], [73.6, 729.0], [73.7, 729.0], [73.8, 730.0], [73.9, 731.0], [74.0, 731.0], [74.1, 732.0], [74.2, 733.0], [74.3, 734.0], [74.4, 735.0], [74.5, 736.0], [74.6, 736.0], [74.7, 737.0], [74.8, 738.0], [74.9, 739.0], [75.0, 739.0], [75.1, 740.0], [75.2, 740.0], [75.3, 740.0], [75.4, 741.0], [75.5, 741.0], [75.6, 742.0], [75.7, 742.0], [75.8, 743.0], [75.9, 743.0], [76.0, 744.0], [76.1, 744.0], [76.2, 745.0], [76.3, 745.0], [76.4, 745.0], [76.5, 746.0], [76.6, 746.0], [76.7, 746.0], [76.8, 747.0], [76.9, 747.0], [77.0, 748.0], [77.1, 748.0], [77.2, 748.0], [77.3, 749.0], [77.4, 749.0], [77.5, 749.0], [77.6, 749.0], [77.7, 750.0], [77.8, 751.0], [77.9, 751.0], [78.0, 752.0], [78.1, 752.0], [78.2, 752.0], [78.3, 754.0], [78.4, 754.0], [78.5, 754.0], [78.6, 755.0], [78.7, 755.0], [78.8, 756.0], [78.9, 756.0], [79.0, 756.0], [79.1, 757.0], [79.2, 757.0], [79.3, 757.0], [79.4, 757.0], [79.5, 758.0], [79.6, 758.0], [79.7, 758.0], [79.8, 759.0], [79.9, 759.0], [80.0, 759.0], [80.1, 760.0], [80.2, 760.0], [80.3, 761.0], [80.4, 761.0], [80.5, 762.0], [80.6, 762.0], [80.7, 763.0], [80.8, 763.0], [80.9, 764.0], [81.0, 765.0], [81.1, 767.0], [81.2, 767.0], [81.3, 767.0], [81.4, 767.0], [81.5, 767.0], [81.6, 768.0], [81.7, 768.0], [81.8, 769.0], [81.9, 769.0], [82.0, 770.0], [82.1, 770.0], [82.2, 770.0], [82.3, 771.0], [82.4, 773.0], [82.5, 774.0], [82.6, 774.0], [82.7, 775.0], [82.8, 776.0], [82.9, 776.0], [83.0, 777.0], [83.1, 778.0], [83.2, 778.0], [83.3, 778.0], [83.4, 779.0], [83.5, 780.0], [83.6, 780.0], [83.7, 783.0], [83.8, 783.0], [83.9, 785.0], [84.0, 786.0], [84.1, 786.0], [84.2, 788.0], [84.3, 789.0], [84.4, 790.0], [84.5, 790.0], [84.6, 793.0], [84.7, 793.0], [84.8, 795.0], [84.9, 796.0], [85.0, 797.0], [85.1, 797.0], [85.2, 799.0], [85.3, 801.0], [85.4, 802.0], [85.5, 802.0], [85.6, 803.0], [85.7, 804.0], [85.8, 804.0], [85.9, 805.0], [86.0, 806.0], [86.1, 806.0], [86.2, 808.0], [86.3, 808.0], [86.4, 809.0], [86.5, 809.0], [86.6, 811.0], [86.7, 812.0], [86.8, 812.0], [86.9, 813.0], [87.0, 814.0], [87.1, 815.0], [87.2, 817.0], [87.3, 817.0], [87.4, 818.0], [87.5, 818.0], [87.6, 820.0], [87.7, 820.0], [87.8, 821.0], [87.9, 821.0], [88.0, 822.0], [88.1, 822.0], [88.2, 822.0], [88.3, 822.0], [88.4, 824.0], [88.5, 824.0], [88.6, 824.0], [88.7, 824.0], [88.8, 824.0], [88.9, 825.0], [89.0, 825.0], [89.1, 825.0], [89.2, 825.0], [89.3, 825.0], [89.4, 826.0], [89.5, 826.0], [89.6, 826.0], [89.7, 826.0], [89.8, 826.0], [89.9, 826.0], [90.0, 827.0], [90.1, 827.0], [90.2, 827.0], [90.3, 827.0], [90.4, 827.0], [90.5, 828.0], [90.6, 828.0], [90.7, 829.0], [90.8, 829.0], [90.9, 829.0], [91.0, 829.0], [91.1, 830.0], [91.2, 830.0], [91.3, 830.0], [91.4, 830.0], [91.5, 831.0], [91.6, 831.0], [91.7, 831.0], [91.8, 831.0], [91.9, 831.0], [92.0, 832.0], [92.1, 832.0], [92.2, 833.0], [92.3, 833.0], [92.4, 833.0], [92.5, 834.0], [92.6, 834.0], [92.7, 836.0], [92.8, 837.0], [92.9, 837.0], [93.0, 837.0], [93.1, 837.0], [93.2, 839.0], [93.3, 840.0], [93.4, 841.0], [93.5, 843.0], [93.6, 843.0], [93.7, 844.0], [93.8, 845.0], [93.9, 845.0], [94.0, 845.0], [94.1, 846.0], [94.2, 847.0], [94.3, 847.0], [94.4, 848.0], [94.5, 850.0], [94.6, 850.0], [94.7, 850.0], [94.8, 851.0], [94.9, 852.0], [95.0, 853.0], [95.1, 854.0], [95.2, 855.0], [95.3, 855.0], [95.4, 855.0], [95.5, 856.0], [95.6, 857.0], [95.7, 858.0], [95.8, 858.0], [95.9, 859.0], [96.0, 860.0], [96.1, 860.0], [96.2, 861.0], [96.3, 861.0], [96.4, 862.0], [96.5, 862.0], [96.6, 863.0], [96.7, 864.0], [96.8, 867.0], [96.9, 868.0], [97.0, 869.0], [97.1, 870.0], [97.2, 871.0], [97.3, 873.0], [97.4, 874.0], [97.5, 876.0], [97.6, 877.0], [97.7, 879.0], [97.8, 881.0], [97.9, 881.0], [98.0, 882.0], [98.1, 883.0], [98.2, 884.0], [98.3, 884.0], [98.4, 885.0], [98.5, 886.0], [98.6, 886.0], [98.7, 887.0], [98.8, 888.0], [98.9, 889.0], [99.0, 891.0], [99.1, 892.0], [99.2, 895.0], [99.3, 896.0], [99.4, 897.0], [99.5, 898.0], [99.6, 899.0], [99.7, 899.0], [99.8, 901.0], [99.9, 901.0]], "isOverall": false, "label": "login Request", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Response Time Percentiles"}},
        getOptions: function() {
            return {
                series: {
                    points: { show: false }
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentiles'
                },
                xaxis: {
                    tickDecimals: 1,
                    axisLabel: "Percentiles",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Percentile value in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : %x.2 percentile was %y ms"
                },
                selection: { mode: "xy" },
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentiles"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesPercentiles"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesPercentiles"), dataset, prepareOverviewOptions(options));
        }
};

/**
 * @param elementId Id of element where we display message
 */
function setEmptyGraph(elementId) {
    $(function() {
        $(elementId).text("No graph series with filter="+seriesFilter);
    });
}

// Response times percentiles
function refreshResponseTimePercentiles() {
    var infos = responseTimePercentilesInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimePercentiles");
        return;
    }
    if (isGraph($("#flotResponseTimesPercentiles"))){
        infos.createGraph();
    } else {
        var choiceContainer = $("#choicesResponseTimePercentiles");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesPercentiles", "#overviewResponseTimesPercentiles");
        $('#bodyResponseTimePercentiles .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimeDistributionInfos = {
        data: {"result": {"minY": 1.0, "minX": 0.0, "maxY": 304.0, "series": [{"data": [[0.0, 58.0], [300.0, 216.0], [600.0, 304.0], [700.0, 294.0], [100.0, 175.0], [200.0, 188.0], [400.0, 224.0], [800.0, 290.0], [1600.0, 1.0], [900.0, 3.0], [500.0, 247.0]], "isOverall": false, "label": "login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 100, "maxX": 1600.0, "title": "Response Time Distribution"}},
        getOptions: function() {
            var granularity = this.data.result.granularity;
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    barWidth: this.data.result.granularity
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " responses for " + label + " were between " + xval + " and " + (xval + granularity) + " ms";
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimeDistribution"), prepareData(data.result.series, $("#choicesResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshResponseTimeDistribution() {
    var infos = responseTimeDistributionInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeDistribution");
        return;
    }
    if (isGraph($("#flotResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var syntheticResponseTimeDistributionInfos = {
        data: {"result": {"minY": 1.0, "minX": 0.0, "ticks": [[0, "Requests having \nresponse time <= 500ms"], [1, "Requests having \nresponse time > 500ms and <= 1,500ms"], [2, "Requests having \nresponse time > 1,500ms"], [3, "Requests in error"]], "maxY": 1135.0, "series": [{"data": [[0.0, 864.0]], "color": "#9ACD32", "isOverall": false, "label": "Requests having \nresponse time <= 500ms", "isController": false}, {"data": [[1.0, 1135.0]], "color": "yellow", "isOverall": false, "label": "Requests having \nresponse time > 500ms and <= 1,500ms", "isController": false}, {"data": [[2.0, 1.0]], "color": "orange", "isOverall": false, "label": "Requests having \nresponse time > 1,500ms", "isController": false}, {"data": [], "color": "#FF6347", "isOverall": false, "label": "Requests in error", "isController": false}], "supportsControllersDiscrimination": false, "maxX": 2.0, "title": "Synthetic Response Times Distribution"}},
        getOptions: function() {
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendSyntheticResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times ranges",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                    tickLength:0,
                    min:-0.5,
                    max:3.5
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    align: "center",
                    barWidth: 0.25,
                    fill:.75
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " " + label;
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            options.xaxis.ticks = data.result.ticks;
            $.plot($("#flotSyntheticResponseTimeDistribution"), prepareData(data.result.series, $("#choicesSyntheticResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshSyntheticResponseTimeDistribution() {
    var infos = syntheticResponseTimeDistributionInfos;
    prepareSeries(infos.data, true);
    if (isGraph($("#flotSyntheticResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerSyntheticResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var activeThreadsOverTimeInfos = {
        data: {"result": {"minY": 70.19200000000005, "minX": 1.79193144E12, "maxY": 70.19200000000005, "series": [{"data": [[1.79193144E12, 70.19200000000005]], "isOverall": false, "label": "Grupo de Usuários", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79193144E12, "title": "Active Threads Over Time"}},
        getOptions: function() {
            return {
                series: {
                    stack: true,
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 6,
                    show: true,
                    container: '#legendActiveThreadsOverTime'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                selection: {
                    mode: 'xy'
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : At %x there were %y active threads"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesActiveThreadsOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotActiveThreadsOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewActiveThreadsOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Active Threads Over Time
function refreshActiveThreadsOverTime(fixTimestamps) {
    var infos = activeThreadsOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotActiveThreadsOverTime"))) {
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesActiveThreadsOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotActiveThreadsOverTime", "#overviewActiveThreadsOverTime");
        $('#footerActiveThreadsOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var timeVsThreadsInfos = {
        data: {"result": {"minY": 86.28571428571429, "minX": 1.0, "maxY": 885.75, "series": [{"data": [[2.0, 106.0], [3.0, 136.0], [4.0, 100.25], [5.0, 88.75], [6.0, 100.5], [7.0, 93.4], [8.0, 86.28571428571429], [9.0, 87.83333333333334], [10.0, 94.83333333333333], [11.0, 90.24999999999999], [12.0, 99.0], [13.0, 100.0], [14.0, 92.42857142857143], [15.0, 124.66666666666667], [16.0, 134.625], [17.0, 130.125], [18.0, 139.0], [19.0, 153.86363636363637], [20.0, 147.67999999999998], [21.0, 164.86666666666667], [22.0, 191.0], [23.0, 171.07142857142858], [24.0, 181.99999999999997], [25.0, 199.78571428571428], [26.0, 193.39999999999998], [27.0, 210.8666666666667], [28.0, 214.81818181818184], [29.0, 209.25000000000003], [30.0, 245.0], [31.0, 231.2941176470588], [32.0, 242.83333333333331], [33.0, 262.5882352941176], [34.0, 281.18181818181813], [35.0, 263.5], [36.0, 272.0769230769231], [37.0, 276.0], [38.0, 305.52941176470586], [39.0, 293.64285714285717], [40.0, 298.8235294117647], [41.0, 295.0], [42.0, 339.43750000000006], [43.0, 307.8181818181818], [44.0, 326.37499999999994], [45.0, 327.43478260869557], [46.0, 387.8666666666667], [47.0, 349.05882352941177], [48.0, 360.75000000000006], [49.0, 383.0], [50.0, 370.9230769230769], [51.0, 384.29411764705884], [52.0, 404.9545454545455], [53.0, 406.1111111111111], [54.0, 388.0], [55.0, 428.59999999999997], [56.0, 431.5238095238095], [57.0, 435.85714285714283], [58.0, 437.42857142857144], [59.0, 464.1111111111111], [60.0, 439.57142857142856], [61.0, 462.2], [62.0, 510.0], [63.0, 456.06666666666666], [64.0, 495.91999999999996], [65.0, 487.4782608695652], [66.0, 493.6363636363637], [67.0, 544.3333333333334], [68.0, 503.578947368421], [69.0, 509.27777777777777], [70.0, 507.9047619047619], [71.0, 563.9565217391305], [72.0, 543.4], [73.0, 566.483870967742], [74.0, 561.3999999999999], [75.0, 541.0000000000001], [76.0, 613.4761904761905], [77.0, 555.8333333333334], [78.0, 600.5294117647059], [79.0, 596.388888888889], [80.0, 600.5652173913041], [81.0, 604.1428571428571], [82.0, 647.75], [83.0, 637.75], [84.0, 614.1333333333333], [85.0, 648.8571428571429], [86.0, 633.8235294117646], [87.0, 661.45], [88.0, 662.1304347826089], [89.0, 709.1578947368421], [90.0, 673.4782608695652], [91.0, 699.578947368421], [92.0, 677.2727272727274], [93.0, 701.3076923076924], [94.0, 697.8636363636363], [95.0, 734.5000000000001], [96.0, 687.9333333333333], [97.0, 737.0000000000001], [98.0, 734.814814814815], [99.0, 769.6774193548387], [100.0, 765.8095238095239], [101.0, 777.5833333333333], [102.0, 788.2], [103.0, 765.846153846154], [104.0, 826.4736842105264], [105.0, 781.5], [106.0, 778.3333333333333], [107.0, 801.4761904761904], [108.0, 763.3888888888889], [109.0, 885.75], [110.0, 806.2666666666667], [111.0, 843.9000000000001], [112.0, 820.5999999999999], [113.0, 824.6216216216216], [114.0, 820.8947368421053], [115.0, 828.1250000000001], [116.0, 807.2391304347824], [117.0, 817.1578947368422], [1.0, 149.0]], "isOverall": false, "label": "login Request", "isController": false}, {"data": [[70.19200000000005, 529.6690000000009]], "isOverall": false, "label": "login Request-Aggregated", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 117.0, "title": "Time VS Threads"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: { noColumns: 2,show: true, container: '#legendTimeVsThreads' },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s: At %x.2 active threads, Average response time was %y.2 ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesTimeVsThreads"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotTimesVsThreads"), dataset, options);
            // setup overview
            $.plot($("#overviewTimesVsThreads"), dataset, prepareOverviewOptions(options));
        }
};

// Time vs threads
function refreshTimeVsThreads(){
    var infos = timeVsThreadsInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTimeVsThreads");
        return;
    }
    if(isGraph($("#flotTimesVsThreads"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTimeVsThreads");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTimesVsThreads", "#overviewTimesVsThreads");
        $('#footerTimeVsThreads .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var bytesThroughputOverTimeInfos = {
        data : {"result": {"minY": 8166.666666666667, "minX": 1.79193144E12, "maxY": 27933.333333333332, "series": [{"data": [[1.79193144E12, 27933.333333333332]], "isOverall": false, "label": "Bytes received per second", "isController": false}, {"data": [[1.79193144E12, 8166.666666666667]], "isOverall": false, "label": "Bytes sent per second", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79193144E12, "title": "Bytes Throughput Over Time"}},
        getOptions : function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity) ,
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Bytes / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendBytesThroughputOverTime'
                },
                selection: {
                    mode: "xy"
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y"
                }
            };
        },
        createGraph : function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesBytesThroughputOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotBytesThroughputOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewBytesThroughputOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Bytes throughput Over Time
function refreshBytesThroughputOverTime(fixTimestamps) {
    var infos = bytesThroughputOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotBytesThroughputOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesBytesThroughputOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotBytesThroughputOverTime", "#overviewBytesThroughputOverTime");
        $('#footerBytesThroughputOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimesOverTimeInfos = {
        data: {"result": {"minY": 529.6690000000009, "minX": 1.79193144E12, "maxY": 529.6690000000009, "series": [{"data": [[1.79193144E12, 529.6690000000009]], "isOverall": false, "label": "login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79193144E12, "title": "Response Time Over Time"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average response time was %y ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Times Over Time
function refreshResponseTimeOverTime(fixTimestamps) {
    var infos = responseTimesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotResponseTimesOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesOverTime", "#overviewResponseTimesOverTime");
        $('#footerResponseTimesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var latenciesOverTimeInfos = {
        data: {"result": {"minY": 529.6385000000005, "minX": 1.79193144E12, "maxY": 529.6385000000005, "series": [{"data": [[1.79193144E12, 529.6385000000005]], "isOverall": false, "label": "login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79193144E12, "title": "Latencies Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response latencies in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendLatenciesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average latency was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesLatenciesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotLatenciesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewLatenciesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Latencies Over Time
function refreshLatenciesOverTime(fixTimestamps) {
    var infos = latenciesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyLatenciesOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotLatenciesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesLatenciesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotLatenciesOverTime", "#overviewLatenciesOverTime");
        $('#footerLatenciesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var connectTimeOverTimeInfos = {
        data: {"result": {"minY": 0.07800000000000011, "minX": 1.79193144E12, "maxY": 0.07800000000000011, "series": [{"data": [[1.79193144E12, 0.07800000000000011]], "isOverall": false, "label": "login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79193144E12, "title": "Connect Time Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getConnectTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average Connect Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendConnectTimeOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average connect time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesConnectTimeOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotConnectTimeOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewConnectTimeOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Connect Time Over Time
function refreshConnectTimeOverTime(fixTimestamps) {
    var infos = connectTimeOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyConnectTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotConnectTimeOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesConnectTimeOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotConnectTimeOverTime", "#overviewConnectTimeOverTime");
        $('#footerConnectTimeOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var responseTimePercentilesOverTimeInfos = {
        data: {"result": {"minY": 64.0, "minX": 1.79193144E12, "maxY": 1643.0, "series": [{"data": [[1.79193144E12, 1643.0]], "isOverall": false, "label": "Max", "isController": false}, {"data": [[1.79193144E12, 827.0]], "isOverall": false, "label": "90th percentile", "isController": false}, {"data": [[1.79193144E12, 890.98]], "isOverall": false, "label": "99th percentile", "isController": false}, {"data": [[1.79193144E12, 853.0]], "isOverall": false, "label": "95th percentile", "isController": false}, {"data": [[1.79193144E12, 64.0]], "isOverall": false, "label": "Min", "isController": false}, {"data": [[1.79193144E12, 552.0]], "isOverall": false, "label": "Median", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79193144E12, "title": "Response Time Percentiles Over Time (successful requests only)"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Response Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentilesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Response time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentilesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimePercentilesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimePercentilesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Time Percentiles Over Time
function refreshResponseTimePercentilesOverTime(fixTimestamps) {
    var infos = responseTimePercentilesOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotResponseTimePercentilesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimePercentilesOverTime", "#overviewResponseTimePercentilesOverTime");
        $('#footerResponseTimePercentilesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var responseTimeVsRequestInfos = {
    data: {"result": {"minY": 80.0, "minX": 68.0, "maxY": 840.0, "series": [{"data": [[131.0, 589.0], [134.0, 239.0], [132.0, 428.0], [133.0, 840.0], [135.0, 839.0], [136.0, 688.0], [68.0, 298.0], [80.0, 80.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 136.0, "title": "Response Time Vs Request"}},
    getOptions: function() {
        return {
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Response Time in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: {
                noColumns: 2,
                show: true,
                container: '#legendResponseTimeVsRequest'
            },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median response time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesResponseTimeVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotResponseTimeVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewResponseTimeVsRequest"), dataset, prepareOverviewOptions(options));

    }
};

// Response Time vs Request
function refreshResponseTimeVsRequest() {
    var infos = responseTimeVsRequestInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimeVsRequest"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeVsRequest");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimeVsRequest", "#overviewResponseTimeVsRequest");
        $('#footerResponseRimeVsRequest .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var latenciesVsRequestInfos = {
    data: {"result": {"minY": 79.5, "minX": 68.0, "maxY": 840.0, "series": [{"data": [[131.0, 589.0], [134.0, 239.0], [132.0, 428.0], [133.0, 840.0], [135.0, 839.0], [136.0, 688.0], [68.0, 298.0], [80.0, 79.5]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 136.0, "title": "Latencies Vs Request"}},
    getOptions: function() {
        return{
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Latency in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: { noColumns: 2,show: true, container: '#legendLatencyVsRequest' },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median Latency time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesLatencyVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotLatenciesVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewLatenciesVsRequest"), dataset, prepareOverviewOptions(options));
    }
};

// Latencies vs Request
function refreshLatenciesVsRequest() {
        var infos = latenciesVsRequestInfos;
        prepareSeries(infos.data);
        if(isGraph($("#flotLatenciesVsRequest"))){
            infos.createGraph();
        }else{
            var choiceContainer = $("#choicesLatencyVsRequest");
            createLegend(choiceContainer, infos);
            infos.createGraph();
            setGraphZoomable("#flotLatenciesVsRequest", "#overviewLatenciesVsRequest");
            $('#footerLatenciesVsRequest .legendColorBox > div').each(function(i){
                $(this).clone().prependTo(choiceContainer.find("li").eq(i));
            });
        }
};

var hitsPerSecondInfos = {
        data: {"result": {"minY": 33.333333333333336, "minX": 1.79193144E12, "maxY": 33.333333333333336, "series": [{"data": [[1.79193144E12, 33.333333333333336]], "isOverall": false, "label": "hitsPerSecond", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79193144E12, "title": "Hits Per Second"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of hits / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendHitsPerSecond"
                },
                selection: {
                    mode : 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y.2 hits/sec"
                }
            };
        },
        createGraph: function createGraph() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesHitsPerSecond"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotHitsPerSecond"), dataset, options);
            // setup overview
            $.plot($("#overviewHitsPerSecond"), dataset, prepareOverviewOptions(options));
        }
};

// Hits per second
function refreshHitsPerSecond(fixTimestamps) {
    var infos = hitsPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if (isGraph($("#flotHitsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesHitsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotHitsPerSecond", "#overviewHitsPerSecond");
        $('#footerHitsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var codesPerSecondInfos = {
        data: {"result": {"minY": 33.333333333333336, "minX": 1.79193144E12, "maxY": 33.333333333333336, "series": [{"data": [[1.79193144E12, 33.333333333333336]], "isOverall": false, "label": "200", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79193144E12, "title": "Codes Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendCodesPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "Number of Response Codes %s at %x was %y.2 responses / sec"
                }
            };
        },
    createGraph: function() {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesCodesPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotCodesPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewCodesPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Codes per second
function refreshCodesPerSecond(fixTimestamps) {
    var infos = codesPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotCodesPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesCodesPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotCodesPerSecond", "#overviewCodesPerSecond");
        $('#footerCodesPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var transactionsPerSecondInfos = {
        data: {"result": {"minY": 33.333333333333336, "minX": 1.79193144E12, "maxY": 33.333333333333336, "series": [{"data": [[1.79193144E12, 33.333333333333336]], "isOverall": false, "label": "login Request-success", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79193144E12, "title": "Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTransactionsPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                }
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTransactionsPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTransactionsPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewTransactionsPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Transactions per second
function refreshTransactionsPerSecond(fixTimestamps) {
    var infos = transactionsPerSecondInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTransactionsPerSecond");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotTransactionsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTransactionsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTransactionsPerSecond", "#overviewTransactionsPerSecond");
        $('#footerTransactionsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var totalTPSInfos = {
        data: {"result": {"minY": 33.333333333333336, "minX": 1.79193144E12, "maxY": 33.333333333333336, "series": [{"data": [[1.79193144E12, 33.333333333333336]], "isOverall": false, "label": "Transaction-success", "isController": false}, {"data": [], "isOverall": false, "label": "Transaction-failure", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79193144E12, "title": "Total Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTotalTPS"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                },
                colors: ["#9ACD32", "#FF6347"]
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTotalTPS"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTotalTPS"), dataset, options);
        // setup overview
        $.plot($("#overviewTotalTPS"), dataset, prepareOverviewOptions(options));
    }
};

// Total Transactions per second
function refreshTotalTPS(fixTimestamps) {
    var infos = totalTPSInfos;
    // We want to ignore seriesFilter
    prepareSeries(infos.data, false, true);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -10800000);
    }
    if(isGraph($("#flotTotalTPS"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTotalTPS");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTotalTPS", "#overviewTotalTPS");
        $('#footerTotalTPS .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

// Collapse the graph matching the specified DOM element depending the collapsed
// status
function collapse(elem, collapsed){
    if(collapsed){
        $(elem).parent().find(".fa-chevron-up").removeClass("fa-chevron-up").addClass("fa-chevron-down");
    } else {
        $(elem).parent().find(".fa-chevron-down").removeClass("fa-chevron-down").addClass("fa-chevron-up");
        if (elem.id == "bodyBytesThroughputOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshBytesThroughputOverTime(true);
            }
            document.location.href="#bytesThroughputOverTime";
        } else if (elem.id == "bodyLatenciesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesOverTime(true);
            }
            document.location.href="#latenciesOverTime";
        } else if (elem.id == "bodyCustomGraph") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCustomGraph(true);
            }
            document.location.href="#responseCustomGraph";
        } else if (elem.id == "bodyConnectTimeOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshConnectTimeOverTime(true);
            }
            document.location.href="#connectTimeOverTime";
        } else if (elem.id == "bodyResponseTimePercentilesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimePercentilesOverTime(true);
            }
            document.location.href="#responseTimePercentilesOverTime";
        } else if (elem.id == "bodyResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeDistribution();
            }
            document.location.href="#responseTimeDistribution" ;
        } else if (elem.id == "bodySyntheticResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshSyntheticResponseTimeDistribution();
            }
            document.location.href="#syntheticResponseTimeDistribution" ;
        } else if (elem.id == "bodyActiveThreadsOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshActiveThreadsOverTime(true);
            }
            document.location.href="#activeThreadsOverTime";
        } else if (elem.id == "bodyTimeVsThreads") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTimeVsThreads();
            }
            document.location.href="#timeVsThreads" ;
        } else if (elem.id == "bodyCodesPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCodesPerSecond(true);
            }
            document.location.href="#codesPerSecond";
        } else if (elem.id == "bodyTransactionsPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTransactionsPerSecond(true);
            }
            document.location.href="#transactionsPerSecond";
        } else if (elem.id == "bodyTotalTPS") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTotalTPS(true);
            }
            document.location.href="#totalTPS";
        } else if (elem.id == "bodyResponseTimeVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeVsRequest();
            }
            document.location.href="#responseTimeVsRequest";
        } else if (elem.id == "bodyLatenciesVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesVsRequest();
            }
            document.location.href="#latencyVsRequest";
        }
    }
}

/*
 * Activates or deactivates all series of the specified graph (represented by id parameter)
 * depending on checked argument.
 */
function toggleAll(id, checked){
    var placeholder = document.getElementById(id);

    var cases = $(placeholder).find(':checkbox');
    cases.prop('checked', checked);
    $(cases).parent().children().children().toggleClass("legend-disabled", !checked);

    var choiceContainer;
    if ( id == "choicesBytesThroughputOverTime"){
        choiceContainer = $("#choicesBytesThroughputOverTime");
        refreshBytesThroughputOverTime(false);
    } else if(id == "choicesResponseTimesOverTime"){
        choiceContainer = $("#choicesResponseTimesOverTime");
        refreshResponseTimeOverTime(false);
    }else if(id == "choicesResponseCustomGraph"){
        choiceContainer = $("#choicesResponseCustomGraph");
        refreshCustomGraph(false);
    } else if ( id == "choicesLatenciesOverTime"){
        choiceContainer = $("#choicesLatenciesOverTime");
        refreshLatenciesOverTime(false);
    } else if ( id == "choicesConnectTimeOverTime"){
        choiceContainer = $("#choicesConnectTimeOverTime");
        refreshConnectTimeOverTime(false);
    } else if ( id == "choicesResponseTimePercentilesOverTime"){
        choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        refreshResponseTimePercentilesOverTime(false);
    } else if ( id == "choicesResponseTimePercentiles"){
        choiceContainer = $("#choicesResponseTimePercentiles");
        refreshResponseTimePercentiles();
    } else if(id == "choicesActiveThreadsOverTime"){
        choiceContainer = $("#choicesActiveThreadsOverTime");
        refreshActiveThreadsOverTime(false);
    } else if ( id == "choicesTimeVsThreads"){
        choiceContainer = $("#choicesTimeVsThreads");
        refreshTimeVsThreads();
    } else if ( id == "choicesSyntheticResponseTimeDistribution"){
        choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        refreshSyntheticResponseTimeDistribution();
    } else if ( id == "choicesResponseTimeDistribution"){
        choiceContainer = $("#choicesResponseTimeDistribution");
        refreshResponseTimeDistribution();
    } else if ( id == "choicesHitsPerSecond"){
        choiceContainer = $("#choicesHitsPerSecond");
        refreshHitsPerSecond(false);
    } else if(id == "choicesCodesPerSecond"){
        choiceContainer = $("#choicesCodesPerSecond");
        refreshCodesPerSecond(false);
    } else if ( id == "choicesTransactionsPerSecond"){
        choiceContainer = $("#choicesTransactionsPerSecond");
        refreshTransactionsPerSecond(false);
    } else if ( id == "choicesTotalTPS"){
        choiceContainer = $("#choicesTotalTPS");
        refreshTotalTPS(false);
    } else if ( id == "choicesResponseTimeVsRequest"){
        choiceContainer = $("#choicesResponseTimeVsRequest");
        refreshResponseTimeVsRequest();
    } else if ( id == "choicesLatencyVsRequest"){
        choiceContainer = $("#choicesLatencyVsRequest");
        refreshLatenciesVsRequest();
    }
    var color = checked ? "black" : "#818181";
    if(choiceContainer != null) {
        choiceContainer.find("label").each(function(){
            this.style.color = color;
        });
    }
}

