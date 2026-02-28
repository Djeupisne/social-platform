from enum import Enum


class CategorieSociale(str, Enum):
    TRES_VULNERABLE = "TRES_VULNERABLE"
    VULNERABLE = "VULNERABLE"
    A_RISQUE = "A_RISQUE"
    NON_VULNERABLE = "NON_VULNERABLE"
    RICHE = "RICHE"
    TRES_RICHE = "TRES_RICHE"

    @property
    def label(self) -> str:
        labels = {
            "TRES_VULNERABLE": "Très vulnérable",
            "VULNERABLE": "Vulnérable",
            "A_RISQUE": "À risque",
            "NON_VULNERABLE": "Non vulnérable",
            "RICHE": "Riche",
            "TRES_RICHE": "Très riche",
        }
        return labels[self.value]

    @property
    def min_score(self) -> int:
        scores = {
            "TRES_VULNERABLE": 0,
            "VULNERABLE": 20,
            "A_RISQUE": 45,
            "NON_VULNERABLE": 56,
            "RICHE": 71,
            "TRES_RICHE": 86,
        }
        return scores[self.value]

    @property
    def max_score(self) -> int:
        scores = {
            "TRES_VULNERABLE": 19,
            "VULNERABLE": 44,
            "A_RISQUE": 55,
            "NON_VULNERABLE": 70,
            "RICHE": 85,
            "TRES_RICHE": 999999,
        }
        return scores[self.value]

    @classmethod
    def from_score(cls, score: int) -> "CategorieSociale":
        if score < 20:
            return cls.TRES_VULNERABLE
        elif score < 45:
            return cls.VULNERABLE
        elif score <= 55:
            return cls.A_RISQUE
        elif score <= 70:
            return cls.NON_VULNERABLE
        elif score <= 85:
            return cls.RICHE
        else:
            return cls.TRES_RICHE
