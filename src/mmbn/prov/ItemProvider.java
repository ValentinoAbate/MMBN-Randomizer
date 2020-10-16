package mmbn.prov;

import mmbn.types.Item;
import mmbn.types.BattleChip;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mmbn.ChipLibrary;
import mmbn.prod.ItemProducer;
import rand.DataProvider;
import rand.RandomizerContext;

public class ItemProvider extends DataProvider<Item> {
	private final ChipLibrary chipLibrary;

	private boolean codeOnly;
	private boolean useCodeMapping;
	private Map<BattleChip, Map<Integer, Byte>> codeMapping;

	public ItemProvider(RandomizerContext context, ItemProducer producer) {
		super(context, producer);
		this.chipLibrary = producer.chipLibrary();
		this.codeOnly = false;
		this.useCodeMapping = false;
		this.codeMapping = new HashMap<BattleChip, Map<Integer, Byte>>();
	}

	public void setCodeOnly(boolean codeOnly) {
		this.codeOnly = codeOnly;
	}

	public void setUseCodeMapping(boolean useCodeMapping) {
		this.useCodeMapping = useCodeMapping;
	}

	@Override
	protected void randomizeData(Random rng, Item item, int position) {
		if (item.isChip()) {
			BattleChip chip = item.getChip();
			int r;

			if (!codeOnly) {
				// Choose a random new battle chip with the same library and rarity.
				List<BattleChip> possibleChips = this.chipLibrary.query(
						(byte) -1,
						chip.getRarity(), chip.getRarity(),
						null,
						chip.getLibrary(),
						0, 99
				);
				if (possibleChips.size() <= 0) {
					possibleChips.add(chip);
				}

				r = rng.nextInt(possibleChips.size());
				chip = possibleChips.get(r);
			}

			// Choose a random code.
			byte[] possibleCodes = chip.getCodes();
			byte newCode = possibleCodes[0];
			// If code only, use the code mapping
			if(!useCodeMapping) {
				r = rng.nextInt(possibleCodes.length);
				newCode = possibleCodes[r];
			}
			else { // Remap the codes (to preserve reward structure)
				if(!codeMapping.containsKey((chip))) {
					codeMapping.put(chip, new HashMap<Integer, Byte>());
				}
				Map<Integer, Byte> map = codeMapping.get(chip);
				int originalCode = item.getCode();
				if(map.containsKey(originalCode)) {
					newCode = map.get(originalCode);
				}
				else
				{
					List<Byte> possibleMapCodes = new ArrayList<Byte>();
					for (byte code : possibleCodes) {
						if(!map.containsValue(code)) {
							possibleMapCodes.add(code);
						}
					}
					if(possibleMapCodes.size() > 0) {
						r = rng.nextInt(possibleMapCodes.size());
						newCode = possibleCodes[r];
						map.put(originalCode, newCode);
					}
				}
			}

			if (item.type() == Item.Type.BATTLECHIP_TRAP) {
				item.setChipCodeTrap(chip, newCode);
			} else {
				item.setChipCode(chip, newCode);
			}
		}
	}
}
