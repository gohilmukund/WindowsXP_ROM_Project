#!/system/bin/sh
while true; do
  if [ -f /sdcard/power_action ]; then
    cmd=$(cat /sdcard/power_action)
    rm -f /sdcard/power_action /data/local/tmp/power_action
    if [ "$cmd" = "shutdown" ]; then
      reboot -p
    else
      reboot
    fi
  elif [ -f /data/local/tmp/power_action ]; then
    cmd=$(cat /data/local/tmp/power_action)
    rm -f /sdcard/power_action /data/local/tmp/power_action
    if [ "$cmd" = "shutdown" ]; then
      reboot -p
    else
      reboot
    fi
  fi
  sleep 0.5
done
