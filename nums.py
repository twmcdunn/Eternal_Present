with open("piano_samples/midiNumMap.txt", 'w') as f1:
    with open("sampleNums.txt") as f:
        line = f.readline()
        while not line == "":
            line = line.rstrip("\n")
            line = line[line.index("=") + 1:len(line)]
            print(line)
            f1.write(line + ",")
            line = f.readline()