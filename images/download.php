<?php

// This script reads some data from the .txt files and fetches
// images from the Arreat Summit website

foreach ([
  '../data/Misc.txt' => '',
  '../data/Weapons.txt' => 'weapons',
  '../data/Armor.txt' => 'armor',
] as $file => $prefix) {
  $fp = fopen($file, 'r');

  if (empty($fp)) {
    die("ERROR: Cannot open file '$file'\n");
  }

  $header = fgetcsv($fp, 0, "\t");

  $nameHeader = array_search("name", $header);
  $codeHeader = array_search("code", $header);
  $normHeader = array_search("normcode", $header);

  while (!feof($fp)) {
    $row = fgetcsv($fp, 0, "\t");
    if ($row === false) { break; }

    $name = trim($row[$nameHeader]);
    $code = trim($row[$codeHeader]);
    $normCode = trim($row[$normHeader]);

    if (strlen($code) !== 3) {
      continue;
    }

    if (strlen($normCode) && $normCode !== $code) {
      continue;
    }

    if (file_exists("$code.gif")) {
      continue;
    }

    $name = strtolower($name);

    if ($prefix === 'armor') {
      $name = str_replace(' armor', '', $name);
    }

    //f ($prefix === '') {
      $name = str_replace(' potion', '', $name);
    //}

    $name = str_replace(' ', '', $name);
    $name = str_replace('\'', '', $name);
    $name = str_replace('-', '', $name);
    $name = trim($name);

    $x = trim("$prefix/$name", '/');
    $image = @file_get_contents("http://classic.battle.net/images/battle/diablo2exp/images/items/$x.gif");

    if ($image === false) {
      echo "Missing image for '$name' with code '$code'\n";
      continue;
    }

    file_put_contents("$code.gif", $image);
  }

  fclose($fp);
}
