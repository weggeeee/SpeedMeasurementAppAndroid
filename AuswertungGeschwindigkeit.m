clear all;
close all;
%Laden aller 3 Datein aus der App
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
pfad    = 'C:\Messung-Messung 1.txt';
pfad2   = 'C:\Messung-Messung 1-SensorDatenAcc.txt';
pfad3   = 'C:\Messung-Messung 1-SensorDatenYaw.txt';
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Einlesen als Tabelle
t               = readtable(pfad);
t2              = readtable(pfad2);
t3              = readtable(pfad3);

% Verarbeitung Loaction Daten
latitude        = table2array(t(:,1));
longitude       = table2array(t(:,2));
circleAccu      = table2array(t(:,3));
speed           = table2array(t(:,4));
speedAccu       = table2array(t(:,5));
bearing         = table2array(t(:,6));
bearingAccu     = table2array(t(:,7));
heigth          = table2array(t(:,8));
verticalAccu    = table2array(t(:,9));
location        = table2array(t(:,10));
provider        = table2array(t(:,11));
satNumber       = table2array(t(:,12));
time            = table2array(t(:,14));
timeConverted   = datenum(time, 'HH:MM:SS.FFF');
timeDevice      = table2array(t(:,15));

% Verarbeitung Acceleration Daten
xAcc            = table2array(t2(:,1));
yAcc            = table2array(t2(:,2));
zAcc            = table2array(t2(:,3));
timeAcc         = table2array(t2(:,4));
timeAccMS       = table2array(t2(:,5));

% Verarbeitung Acceleration Daten
xYaw            = table2array(t3(:,1));
yYaw            = table2array(t3(:,2));
zYaw            = table2array(t3(:,3));
timeYaw         = table2array(t3(:,4));
timeYawMS       = table2array(t3(:,5));

% Errechne Zeit ab Startzeitpunkt
timeGone(1) = 0;
for(i=1:length(timeConverted))
    if(i >1)
        if(timeConverted(i-1) ~= timeConverted(i))
            timeGone(i,1) = timeConverted(i) - timeConverted(1); 
        end
    end
end
% Errechne Zeit ab Startzeitpunkt
timeYawGone(1) = 0;
for(i=1:length(timeYawMS))
    if(i >1)
        if(timeYawMS(i-1) ~= timeYawMS(i))
            timeYawGone(i,1) = timeYawMS(i) - timeYawMS(1);
        end
    end
end
% Errechne Zeit ab Startzeitpunkt
timeAccGone(1) = 0;
for(i=1:length(timeAccMS))
    if(i >1)
        if(timeAccMS(i-1) ~= timeAccMS(i))
            timeAccGone(i,1) = timeAccMS(i) - timeAccMS(1); 
        end
    end
end
%Provider numerisch
if(length(provider) ~= 0)
    for i = 1: length(provider)
        if(strcmp(provider(i),'gps'))
            providerInt(i,1) = 1;
        elseif(strcmp(provider(i),'network'))
            providerInt(i,1) = 2;
        end
    end
else
    providerInt = 0;
end

%Finde Beginn der Fahrt in beiden messungen
idx = find(Auto.A_FlexRay__V_VEH__V_VEH_COG~=0, 1, 'first');
idx2 = find(speed~=0, 1, 'first');

%Schneide Vektoren entsprechend zu
speedCut = speed((idx2):end);
timeConverted = timeConverted((idx2):end)*100000;
timeDeviceCut = timeDevice((idx2):end);
speedAuto = Auto.A_FlexRay__V_VEH__V_VEH_COG(idx:end);
timeAuto = Auto.Time(idx:end);

%Nehme ersten Zeitstempel als Sekunde 0 und errechne Zeit die seit beginn
%der Messung vergangen ist
timeGoneRdy(1) = 0;
for(i=1:length(timeConverted))
    if(i >1)
        if(timeConverted(i-1) ~= timeConverted(i))
            timeGoneRdy(i,1) = timeConverted(i) - timeConverted(1); 
        end
    end
end

timeDeviceRdy(1) = 0;
for(i=1:length(timeDeviceCut))
    if(i >1)
        if(timeDeviceCut(i-1) ~= timeDeviceCut(i))
            timeDeviceRdy(i,1) = timeDeviceCut(i) - timeDeviceCut(1); 
        end
    end
end
timeDeviceGone(1) = 0;
for(i=1:length(timeDevice))
    if(i >1)
        if(timeDevice(i-1) ~= timeDevice(i))
            timeDeviceGone(i,1) = timeDevice(i) - timeDevice(1); 
        end
    end
end

timeAutoRdy(1) = 0;
for(i=1:length(timeAuto))
    if(i >1)
        if(timeAuto(i-1) ~= timeAuto(i))
            timeAutoRdy(i,1) = timeAuto(i) - timeAuto(1); 
        end
    end
end

%Stelle alles in plots dar
figure('name', 'Allgemeiner Überblick');
ax1(1) = subplot(9,1,1);
plot(timeDeviceGone, circleAccu);
xlabel('ms');
title('Genauigkeit Standort [m]');
ax1(2) = subplot(9,1,2);
plot(timeDeviceGone, speed);
xlabel('ms');
title('Geschwindigkeit [m/s]');
ax1(3) = subplot(9,1,3);
plot(timeDeviceGone, speedAccu);
xlabel('ms');
title('Genauigkeit Geschwindigkeit [m/s]');
ax1(4) = subplot(9,1,4);
plot(timeDeviceGone, bearing);
xlabel('ms');
title('Winkel [Grad]');
ax1(5) = subplot(9,1,5);
plot(timeDeviceGone, bearingAccu);
xlabel('ms');
title('Genauigkeit Winkel [Grad]');
ax1(6) = subplot(9,1,6);
plot(timeDeviceGone, heigth);
xlabel('ms');
title('Höhe [m]');
ax1(7) = subplot(9,1,7);
plot(timeDeviceGone, verticalAccu);
xlabel('ms');
title('Genauigkeit Höhe [m]');
ax1(8) = subplot(9,1,8);
plot(timeDeviceGone, providerInt);
xlabel('ms');
title('Signalquelle 1=GPS 2=Network');
ax1(9) = subplot(9,1,9);
plot(timeDeviceGone, satNumber);
xlabel('ms');
title('Satelliten Anzahl');
linkaxes(ax1,'x')

figure('name', 'Geschwindigkeitsspezifischer Überblick');
ax2(1) = subplot(4,1,1);
plot(timeDeviceGone, speed);
xlabel('ms');
title('Geschwindigkeit [m/s]');
ax2(2) = subplot(4,1,2);
plot(timeDeviceGone, speedAccu);
xlabel('ms');
title('Genauigkeit Geschwindigkeit [m/s]');
ax2(3) = subplot(4,1,3);
plot(timeDeviceGone, providerInt);
xlabel('ms');
title('Signalquelle 1=GPS 2=Network');
ax2(4) = subplot(4,1,4);
plot(timeDeviceGone, satNumber);
xlabel('ms');
title('Satelliten Anzahl');
linkaxes(ax2,'x')

figure('name', 'Weltkarte');
geoshow('landareas.shp');
geoshow(latitude, longitude, 'DisplayType', 'Point', 'Marker', '+', 'Color', 'red');

figure('name', 'Acc');
ax3(1) = subplot(3,1,1);
plot(timeAccGone, xAcc);
xlabel('ms');
title('Acceleration in x direction [m/s^2]');
ax3(2) = subplot(3,1,2);
plot(timeAccGone, yAcc);
xlabel('ms');
title('Acceleration in y direction [m/s^2]');
ax3(3) = subplot(3,1,3);
plot(timeAccGone, zAcc);
xlabel('ms');
title('Acceleration in z direction [m/s^2]');
linkaxes(ax3,'x');

figure('name', 'Yaw');
ax4(1) = subplot(3,1,1);
plot(timeYawGone, xYaw);
xlabel('ms');
title('Yaw in x direction [rad/s]');
ax4(2) = subplot(3,1,2);
plot(timeYawGone, yYaw);
xlabel('ms');
title('Yaw in y direction [rad/s]');
ax4(3) = subplot(3,1,3);
plot(timeYawGone, zYaw);
xlabel('ms');
title('Yaw in z direction [rad/s]');
linkaxes(ax4,'x')

% %Bei Vergleich Auto/App einkommentieren 
% %Interpoliere Geschwindigkeit App auf Zeitleiste von Messung
% speedCut = interp1(timeGoneRdy, speedCut, timeAutoRdy, 'linear', 'extrap')
% figure; 
% plot(timeAutoRdy, speedAuto/3.6);
% hold all; 
% plot(timeAutoRdy, speed); ylabel('m/s');
% legend('Auto Speed', 'Handy Speed');
